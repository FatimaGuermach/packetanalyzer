package services;

import model.PacketEntity;
import model.LogEntity;
import repository.PacketRepository;
import repository.LogRepository;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TrafficCaptureService {
    private final PacketRepository packetRepository;
    private final LogRepository logRepository;
    private volatile boolean capturing = false;
    private int currentLogId;

    private int ipv4Count = 0;
    private int tcpCount = 0;
    private int udpCount = 0;
    private long totalDataSize = 0;
    private int totalPacketsCaptured = 0;

    public TrafficCaptureService(PacketRepository packetRepository, LogRepository logRepository) {
        this.packetRepository = packetRepository;
        this.logRepository = logRepository;
    }

    public int getIpv4Count() { return ipv4Count; }
    public int getTcpCount() { return tcpCount; }
    public int getUdpCount() { return udpCount; }
    public long getTotalDataSize() { return totalDataSize; }
    public int getTotalPacketsCaptured() { return totalPacketsCaptured; }

    public void startCapture() {
        capturing = true;

        try {
            currentLogId = logRepository.getCurrentLogId();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve current log ID", e);
        }

        new Thread(() -> {
            try {
                PcapNetworkInterface nif = getWifiInterface();
                if (nif == null) {
                    System.out.println("No suitable network interface found.");
                    return;
                }

                try (PcapHandle handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10)) {
                    handle.loop(-1, (PacketListener) packet -> {
                        if (!capturing) {
                            try {
                                handle.breakLoop();
                            } catch (NotOpenException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }

                        PacketEntity entity = parsePacket(packet);
                        if (entity != null) {
                            try {
                                // Associer le paquet au log de la session actuelle
                                entity.setLogId(currentLogId);
                                packetRepository.createPacket(entity);
                                updateStatistics(packet);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            } catch (PcapNativeException | NotOpenException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopCapture() {
        capturing = false;
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


    private PacketEntity parsePacket(Packet packet) {
        try {
            String sourceIp = "Unknown";
            String destinationIp = "Unknown";
            int packetSize = packet.length();
            String protocol = "Unknown";
            Integer sourcePort = null;
            Integer destinationPort = null;

            if (packet.contains(IpV4Packet.class)) {
                IpV4Packet ipv4Packet = packet.get(IpV4Packet.class);
                sourceIp = ipv4Packet.getHeader().getSrcAddr().toString();
                destinationIp = ipv4Packet.getHeader().getDstAddr().toString();
                protocol = ipv4Packet.getHeader().getProtocol().toString();
            }

            if (packet.contains(TcpPacket.class)) {
                TcpPacket tcpPacket = packet.get(TcpPacket.class);
                sourcePort = tcpPacket.getHeader().getSrcPort().valueAsInt();
                destinationPort = tcpPacket.getHeader().getDstPort().valueAsInt();
                protocol = "TCP";
            } else if (packet.contains(UdpPacket.class)) {
                UdpPacket udpPacket = packet.get(UdpPacket.class);
                sourcePort = udpPacket.getHeader().getSrcPort().valueAsInt();
                destinationPort = udpPacket.getHeader().getDstPort().valueAsInt();
                protocol = "UDP";
            }

            return new PacketEntity(0, sourceIp, destinationIp, packetSize, protocol, sourcePort, destinationPort,
                    LocalDateTime.now(), currentLogId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private synchronized void updateStatistics(Packet packet) {
        totalPacketsCaptured++;
        totalDataSize += packet.length();

        if (packet.contains(IpV4Packet.class)) {
            ipv4Count++;
        }
        if (packet.contains(TcpPacket.class)) {
            tcpCount++;
        } else if (packet.contains(UdpPacket.class)) {
            udpCount++;
        }
    }

    public boolean isCapturing() {
        return capturing;
    }
}
