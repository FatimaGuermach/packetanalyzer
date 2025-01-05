package model;

public class PacketEntity {
    private String sourceIP;
    private String destinationIP;
    private int packetSize;
    private String protocol;
    private long timestamp;
    private String sourcePort;
    private String destinationPort;

    public PacketEntity(String sourceIP, String destinationIP, int packetSize, String protocol, long timestamp, String sourcePort, String destinationPort) {
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.packetSize = packetSize;
        this.protocol = protocol;
        this.timestamp = timestamp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    public String getSourceIP() { return sourceIP; }
    public String getDestinationIP() { return destinationIP; }
    public int getPacketSize() { return packetSize; }
    public String getProtocol() { return protocol; }
    public long getTimestamp() { return timestamp; }
    public String getSourcePort() { return sourcePort; }
    public String getDestinationPort() { return destinationPort; }

    // Convert to raw CSV format
    public String toRaw() {
        return sourceIP + "," + destinationIP + "," + packetSize + "," + protocol + "," + sourcePort + "," + destinationPort + "," + timestamp;
    }

    @Override
    public String toString() {
        return "Packet from " + sourceIP + " to " + destinationIP + " size: " + packetSize + " bytes, protocol: " + protocol + ", source port: " + sourcePort + ", destination port: " + destinationPort + ", timestamp: " + timestamp;
    }
}
