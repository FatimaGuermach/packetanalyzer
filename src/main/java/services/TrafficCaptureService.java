package services;

import model.PacketEntity;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class TrafficCaptureService {
    private PcapHandle pcapHandle;
    private String csvFilePath;
    private BufferedWriter writer;

    public TrafficCaptureService(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        try {
            // Initialize the writer here, so we don't open it repeatedly in every packet capture
            writer = new BufferedWriter(new FileWriter(csvFilePath, true)); // 'true' appends to the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start capturing traffic and storing it in CSV
    public void startCapture() {
        Thread captureThread = new Thread(() -> {
            try {
                List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
                if (devices.isEmpty()) {
                    System.out.println("No devices found!");
                    return;
                }

                // Print all available devices and select one
                for (PcapNetworkInterface device : devices) {
                    System.out.println("Device: " + device.getName());
                }

                // Select the first device for example; replace this with your device selection logic
                PcapNetworkInterface device = devices.get(6);  // Or select a specific device by name
                if (device == null) {
                    System.out.println("Device not found.");
                    return;
                }

                pcapHandle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1000);

                // Write the header if the file is empty (i.e., first capture)
                if (new java.io.File(csvFilePath).length() == 0) {
                    writer.write("Source IP,Destination IP,Packet Size,Source Port,Destination Port,Protocol,Timestamp");
                    writer.newLine();
                }

                // Start capturing packets and write them to CSV using lambda
                pcapHandle.loop(Integer.MAX_VALUE, (PacketListener) packet -> writePacketToCSV(packet));

            } catch (PcapNativeException | NotOpenException | IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        captureThread.start();  // Start the packet capture on a new thread
    }



    // Function to write packet details to CSV using the PacketEntity class and toRaw method
    private void writePacketToCSV(Packet packet) {
        String sourceIP = "Unknown";
        String destinationIP = "Unknown";
        int packetSize = packet.length();
        String protocol = "Unknown";
        long timestamp = new Date().getTime();
        int sourcePort = -1;
        int destinationPort = -1;

        // Example of extracting IP address and protocol
        if (packet.contains(org.pcap4j.packet.IpPacket.class)) {
            org.pcap4j.packet.IpPacket ipPacket = packet.get(org.pcap4j.packet.IpPacket.class);
            sourceIP = ipPacket.getHeader().getSrcAddr().toString();
            destinationIP = ipPacket.getHeader().getDstAddr().toString();
            protocol = ipPacket.getHeader().getProtocol().toString();
        }
        if (packet.contains(TcpPacket.class)) {
            TcpPacket tcpPacket = packet.get(TcpPacket.class);
            sourcePort = tcpPacket.getHeader().getSrcPort().valueAsInt();
            destinationPort = tcpPacket.getHeader().getDstPort().valueAsInt();
        } else if (packet.contains(UdpPacket.class)) {
            UdpPacket udpPacket = packet.get(UdpPacket.class);
            sourcePort = udpPacket.getHeader().getSrcPort().valueAsInt();
            destinationPort = udpPacket.getHeader().getDstPort().valueAsInt();
        }

        // Create a PacketEntity object
        PacketEntity packetEntity = new PacketEntity(sourceIP, destinationIP, packetSize, protocol, sourcePort,destinationPort,timestamp);

        // Write the raw data of the packet to CSV using the toRaw method
        try {
            writer.write(packetEntity.toRaw());
            writer.newLine();
            writer.flush();

            System.out.println("writn--------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stop the capture and close the file

    public void stopCapture() {
        try {
            if (pcapHandle != null) {
                pcapHandle.breakLoop();  // This will stop the loop and capture thread
                pcapHandle.close();
                System.out.println("Capture stopped.");
            }
            if (writer != null) {
                writer.close();  // Ensure the writer is flushed before closing
                System.out.println("CSV file closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int tcpCount = 0;
    private int udpCount = 0;
    private int totalPacketsCaptured = 0;
    private long totalDataSize = 0;
    private boolean capturing = false;

    public boolean isCapturing() {
        return capturing;
    }

    public int getTcpCount() {
        return tcpCount;
    }

    public int getUdpCount() {
        return udpCount;
    }

    public int getTotalPacketsCaptured() {
        return totalPacketsCaptured;
    }

    public long getTotalDataSize() {
        return totalDataSize;
    }






}
