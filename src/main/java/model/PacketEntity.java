package model;

public class PacketEntity {
    private String sourceIP;
    private String destinationIP;
    private int packetSize;
    private String protocol;
    private long timestamp;

    public PacketEntity(String sourceIP, String destinationIP, int packetSize, String protocol, long timestamp) {
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.packetSize = packetSize;
        this.protocol = protocol;
        this.timestamp = timestamp;
    }

    public String getSourceIP() { return sourceIP; }
    public String getDestinationIP() { return destinationIP; }
    public int getPacketSize() { return packetSize; }
    public String getProtocol() { return protocol; }
    public long getTimestamp() { return timestamp; }

    // Function to return packet data as a raw comma-separated string
    public String toRaw() {
        return sourceIP + "," + destinationIP + "," + packetSize + "," + protocol + "," + timestamp;
    }

    @Override
    public String toString() {
        return "Packet from " + sourceIP + " to " + destinationIP + " size: " + packetSize + " bytes, protocol: " + protocol + ", timestamp: " + timestamp;
    }
}
