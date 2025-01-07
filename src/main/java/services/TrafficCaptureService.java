package services;

import model.PacketEntity;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import java.io.*;
import java.util.Date;
import java.util.List;

public class TrafficCaptureService {
    private PcapHandle pcapHandle;
    private String csvFilePath;
    private BufferedWriter writer;
    private Thread captureThread;
    private volatile boolean running = false;

    private int tcpCount = 0;
    private int udpCount = 0;
    private int ipv4Count = 0;
    private int totalPacketsCaptured = 0;
    private long totalDataSize = 0;

    public TrafficCaptureService(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        try {
            writer = new BufferedWriter(new FileWriter(csvFilePath, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startCapture() {
        if (running) {
            System.out.println("Capture is already running.");
            return;
        }

        running = true;

        captureThread = new Thread(() -> {
            try {
                PcapNetworkInterface device = getWifiInterface();
                if (device == null) {
                    System.out.println("Device not found.");
                    return;
                }

                pcapHandle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1000);

                // Write header if the CSV file is empty
                if (new File(csvFilePath).length() == 0) {
                    writer.write("Source IP,Destination IP,Packet Size,Source Port,Destination Port,Protocol,Timestamp");
                    writer.newLine();
                }

                // Start capturing packets
                pcapHandle.loop(0, (PacketListener) packet -> {
                    if (!running) {
                        try {
                            pcapHandle.breakLoop();
                        } catch (NotOpenException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }
                    try {
                        writePacketToCSV(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (PcapNativeException | NotOpenException | IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                stopCapture();
            }
        });

        captureThread.start();
    }

    private void writePacketToCSV(Packet packet) {
        String sourceIP = "Unknown";
        String destinationIP = "Unknown";
        int packetSize = packet.length();
        String protocol = "Unknown";
        long timestamp = new Date().getTime();
        int sourcePort = -1;
        int destinationPort = -1;

        if (packet.contains(IpV4Packet.class)) {
            var ipPacket = packet.get(IpV4Packet.class);
            sourceIP = ipPacket.getHeader().getSrcAddr().toString();
            destinationIP = ipPacket.getHeader().getDstAddr().toString();
            protocol = ipPacket.getHeader().getProtocol().toString();
            ipv4Count++;
        }

        if (packet.contains(TcpPacket.class)) {
            var tcpPacket = packet.get(TcpPacket.class);
            sourcePort = tcpPacket.getHeader().getSrcPort().valueAsInt();
            destinationPort = tcpPacket.getHeader().getDstPort().valueAsInt();
            protocol = "TCP";
            tcpCount++;
        } else if (packet.contains(UdpPacket.class)) {
            var udpPacket = packet.get(UdpPacket.class);
            sourcePort = udpPacket.getHeader().getSrcPort().valueAsInt();
            destinationPort = udpPacket.getHeader().getDstPort().valueAsInt();
            protocol = "UDP";
            udpCount++;
        }

        totalPacketsCaptured++;
        totalDataSize += packetSize;

        PacketEntity packetEntity = new PacketEntity(sourceIP, destinationIP, packetSize, protocol, sourcePort, destinationPort, timestamp);

        try {
            writer.write(packetEntity.toRaw());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCapture() {
        running = false;

        try {
            if (pcapHandle != null && pcapHandle.isOpen()) {
                pcapHandle.breakLoop();
                pcapHandle.close();
            }

            if (writer != null) {
                writer.close();
            }

            if (captureThread != null && captureThread.isAlive()) {
                captureThread.interrupt();
            }

            System.out.println("Capture stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            throw new RuntimeException(e);
        }
    }

    private PcapNetworkInterface getWifiInterface() {
        try {
            // Étape 1 : Exécuter "netsh wlan show interfaces" pour obtenir le GUID de l'interface Wi-Fi
            String wifiGuid = null;
            Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // Recherche d'une ligne contenant "Interface GUID" (ou équivalent selon la langue)
                if (line.toLowerCase().contains("guid")) {
                    wifiGuid = line.split(":")[1].trim(); // Extraire le GUID
                    break;
                }
            }

            // Si aucun GUID n'a été trouvé
            if (wifiGuid == null) {
                System.out.println("GUID de l'interface Wi-Fi non trouvé !");
                return null;
            }

            System.out.println("GUID Wi-Fi détecté : " + wifiGuid);

            // Étape 2 : Comparer avec les interfaces listées par Pcap4J
            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            if (allDevs == null || allDevs.isEmpty()) {
                System.out.println("Aucune interface réseau trouvée !");
                return null;
            }

            // Listening all the available net interfaces
            System.out.println("------------------- Interfaces List -------------------------------");
            for (int i = 0; i < allDevs.size(); i++) {
                PcapNetworkInterface device = allDevs.get(i);
                System.out.println(i + ": " + device.getName() + " :" + device.getDescription() );
            }

            // selecting the wifi interface
            for (PcapNetworkInterface nif : allDevs) {
                if (nif.getName().toLowerCase().contains(wifiGuid.toLowerCase())) {
                    System.out.println("using : "  + nif.getDescription() );
                    return nif; // Correspondance trouvée
                }
            }

            // Si aucune correspondance
            System.out.println("Aucune interface Pcap4J ne correspond au GUID Wi-Fi !");
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isCapturing() {
        return running;
    }

    public int getTcpCount() {
        return tcpCount;
    }

    public int getUdpCount() {
        return udpCount;
    }

    public int getIpv4Count() {
        return ipv4Count;
    }

    public int getTotalPacketsCaptured() {
        return totalPacketsCaptured;
    }

    public long getTotalDataSize() {
        return totalDataSize;
    }
}
