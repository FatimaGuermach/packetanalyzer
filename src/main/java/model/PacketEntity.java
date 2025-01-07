package model;

public class PacketEntity {
    private String sourceIP;
    private String destinationIP;
    private int packetSize;
    private String protocol;
    private long timestamp;
    private int sourcePort;
    private int destinationPort;
    public PacketEntity(String sourceIP, String destinationIP, int packetSize, String protocol, int sourcePort,int destinationPort, long timestamp) {
        this.sourceIP = sourceIP;
        this.destinationPort = destinationPort;
        this.packetSize = packetSize;
        this.sourcePort=sourcePort;
        this.destinationIP=destinationIP;
        this.protocol = protocol;
        this.timestamp = timestamp;
    }

    public String getSourceIP() { return sourceIP; }
    public String getDestinationIP() { return destinationIP; }
    public int getPacketSize() { return packetSize; }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public String getProtocol() { return protocol; }
    public long getTimestamp() { return timestamp; }

    // Function to return packet data as a raw comma-separated string
    public String toRaw() {
        return sourceIP + "," + destinationIP + "," + packetSize + ","+ sourcePort + ","+ destinationPort +"," +protocol + "," + timestamp;
    }

    @Override
    public String toString() {
        return "Packet from " + sourceIP + " to " + destinationIP + " size: " + packetSize + " bytes, protocol: " + protocol + ", timestamp: " + timestamp;
    }
}
