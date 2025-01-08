package services;

import model.PacketEntity;
import org.pcap4j.packet.Packet;

import java.util.*;
import java.util.stream.Collectors;

public class TrafficAnalysisService {
    private int totalPackets;
    private long totalTrafficVolume;
    private Map<String, Integer> protocolDistribution;
    private Map<String, Integer> sourceIPCount;
    private Map<String, Integer> destinationIPCount;
    private Map<String, Integer> sourcePortCount;
    private Map<String, Integer> destinationPortCount;
    private List<Integer> packetSizes;
    private List<PacketEntity> packets;
    public TrafficAnalysisService() {
        this.totalPackets = 0;
        this.totalTrafficVolume = 0;
        this.protocolDistribution = new HashMap<>();
        this.sourceIPCount = new HashMap<>();
        this.destinationIPCount = new HashMap<>();
        this.sourcePortCount = new HashMap<>();
        this.destinationPortCount = new HashMap<>();
        this.packetSizes = new ArrayList<>();
        this.packets = new ArrayList<>();
    }

    // Method to analyze a captured packet and update statistics
    public void analyzePacket(PacketEntity packetEntity) {
        // Update total packet count and traffic volume
        packets.add(packetEntity);
        totalPackets++;
        totalTrafficVolume += packetEntity.getPacketSize();

        // Update protocol distribution
        protocolDistribution.put(packetEntity.getProtocol(),
                protocolDistribution.getOrDefault(packetEntity.getProtocol(), 0) + 1);

        // Update source and destination IP counts
        sourceIPCount.put(packetEntity.getSourceIP(),
                sourceIPCount.getOrDefault(packetEntity.getSourceIP(), 0) + 1);
        destinationIPCount.put(packetEntity.getDestinationIP(),
                destinationIPCount.getOrDefault(packetEntity.getDestinationIP(), 0) + 1);

        // Update source and destination port counts
//        sourcePortCount.put(packetEntity.getSourcePort(),
//                sourcePortCount.getOrDefault(packetEntity.getSourcePort(), 0) + 1);
//        destinationPortCount.put(packetEntity.getDestinationPort(),
//                destinationPortCount.getOrDefault(packetEntity.getDestinationPort(), 0) + 1);

        // Add packet size to the list
        packetSizes.add(packetEntity.getPacketSize());
    }

    // Get the total number of packets
    public int getTotalPackets() {
        return totalPackets;
    }

    // Get the total traffic volume (in bytes)
    public long getTotalTrafficVolume() {
        return totalTrafficVolume;
    }

    // Get the protocol distribution (percentage of each protocol)
    public Map<String, Integer> getProtocolDistribution() {
        return protocolDistribution;
    }

    // Get the top source IPs (by frequency)
    public List<Map.Entry<String, Integer>> getTopSourceIPs(int topN) {
        return sourceIPCount.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Get the top destination IPs (by frequency)
    public List<Map.Entry<String, Integer>> getTopDestinationIPs(int topN) {
        return destinationIPCount.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Get the top source ports (by frequency)
    public List<Map.Entry<String, Integer>> getTopSourcePorts(int topN) {
        return sourcePortCount.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Get the top destination ports (by frequency)
    public List<Map.Entry<String, Integer>> getTopDestinationPorts(int topN) {
        return destinationPortCount.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Get average packet size
    public double getAveragePacketSize() {
        if (packetSizes.isEmpty()) return 0.0;
        return packetSizes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    // Get the smallest packet size
    public int getSmallestPacketSize() {
        return packetSizes.stream().min(Integer::compareTo).orElse(0);
    }

    // Get the largest packet size
    public int getLargestPacketSize() {
        return packetSizes.stream().max(Integer::compareTo).orElse(0);
    }

    // Print all statistics in a human-readable format
    public void printStatistics() {
        System.out.println("Total Packets: " + totalPackets);
        System.out.println("Total Traffic Volume (bytes): " + totalTrafficVolume);
        System.out.println("Average Packet Size: " + getAveragePacketSize());
        System.out.println("Smallest Packet Size: " + getSmallestPacketSize());
        System.out.println("Largest Packet Size: " + getLargestPacketSize());

        System.out.println("Protocol Distribution:");
        protocolDistribution.forEach((protocol, count) ->
                System.out.println(protocol + ": " + count + " packets"));

        System.out.println("Top Source IPs:");
        getTopSourceIPs(5).forEach(entry ->
                System.out.println(entry.getKey() + ": " + entry.getValue() + " packets"));

        System.out.println("Top Destination IPs:");
        getTopDestinationIPs(5).forEach(entry ->
                System.out.println(entry.getKey() + ": " + entry.getValue() + " packets"));

        System.out.println("Top Source Ports:");
        getTopSourcePorts(5).forEach(entry ->
                System.out.println(entry.getKey() + ": " + entry.getValue() + " packets"));

        System.out.println("Top Destination Ports:");
        getTopDestinationPorts(5).forEach(entry ->
                System.out.println(entry.getKey() + ": " + entry.getValue() + " packets"));
    }
}
