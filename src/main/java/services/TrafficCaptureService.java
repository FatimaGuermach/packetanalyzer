package services;

import model.PacketEntity;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrafficCaptureService {
    private PcapHandle pcapHandle;
    private String csvFilePath;
    private BufferedWriter writer;
    private List<PacketEntity> packets;  // Use a mutable list
    public TrafficCaptureService(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        this.packets = new ArrayList<>();  // Initialize the packets list as an ArrayList
        try {
            writer = new BufferedWriter(new FileWriter(csvFilePath, true)); // 'true' appends to the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start capturing traffic and storing it in CSV
    public void startCapture() {
        try {
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
            if (devices.isEmpty()) {
                System.out.println("No network devices found!");
                return;
            }

            // Print all available devices and select one
            for (PcapNetworkInterface device : devices) {
                System.out.println("Device: " + device.getName());
            }

            // Select the first device for example; replace this with your device selection logic
            PcapNetworkInterface device = devices.get(0);
            if (device == null) {
                System.out.println("Device not found.");
                return;
            }

            pcapHandle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 1000);

            // Write the header if the file is empty (i.e., first capture)
            if (new java.io.File(csvFilePath).length() == 0) {
                writer.write("Source IP,Destination IP,Packet Size,Protocol,Source Port,Destination Port,Timestamp");
                writer.newLine();
            }

            // Start capturing packets and write them to CSV
            pcapHandle.loop(Integer.MAX_VALUE, new PacketListener() {
                @Override
                public void gotPacket(Packet packet) {
                    writePacketToCSV(packet);  // Write the packet to the CSV file
                }
            });

        } catch (PcapNativeException | NotOpenException | IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Function to write packet details to CSV using the PacketEntity class and toRaw method
    private void writePacketToCSV(Packet packet) {
        String sourceIP = "Unknown";
        String destinationIP = "Unknown";
        int packetSize = packet.length();
        String protocol = "Unknown";
        long timestamp = new Date().getTime();
        String sourcePort = "-1";
        String destinationPort = "-1";

        // Example of extracting IP address and protocol
        if (packet.contains(org.pcap4j.packet.IpPacket.class)) {
            org.pcap4j.packet.IpPacket ipPacket = packet.get(org.pcap4j.packet.IpPacket.class);
            sourceIP = ipPacket.getHeader().getSrcAddr().toString();
            destinationIP = ipPacket.getHeader().getDstAddr().toString();
            protocol = ipPacket.getHeader().getProtocol().toString();

            // Extract TCP or UDP ports if the protocol is TCP or UDP
            if (packet.contains(TcpPacket.class)) {
                TcpPacket tcpPacket = packet.get(TcpPacket.class);
                sourcePort = tcpPacket.getHeader().getSrcPort().valueAsString();
                destinationPort = tcpPacket.getHeader().getDstPort().valueAsString();
            } else if (packet.contains(UdpPacket.class)) {
                UdpPacket udpPacket = packet.get(UdpPacket.class);
                sourcePort = udpPacket.getHeader().getSrcPort().valueAsString();
                destinationPort = udpPacket.getHeader().getDstPort().valueAsString();
            }
        }

        // Create a PacketEntity object
        PacketEntity packetEntity = new PacketEntity(sourceIP, destinationIP, packetSize, protocol, timestamp, sourcePort, destinationPort);
        packets.add(packetEntity);

        // Write the raw data of the packet to CSV using the toRaw method
        try {
            writer.write(packetEntity.toRaw());  // Ensure the toRaw method is implemented in PacketEntity
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Stop the capture and close the file
    public void stopCapture() {
        try {
            if (pcapHandle != null) {
                pcapHandle.close();
                System.out.println("Capture stopped.");
            }
            if (writer != null) {
                writer.close();
                System.out.println("CSV file closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
