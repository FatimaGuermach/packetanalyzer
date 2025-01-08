package model;

import java.time.LocalDateTime;

public class PacketEntity {
    private int id;
    private String sourceIp;
    private String destinationIp;
    private int packetSize;
    private String protocol;
    private Integer sourcePort; // Nullable
    private Integer destinationPort; // Nullable
    private LocalDateTime timestamp;
    private int logId;

    public PacketEntity() {}

    public PacketEntity(int id, String sourceIp, String destinationIp, int packetSize, String protocol,
                        Integer sourcePort, Integer destinationPort, LocalDateTime timestamp, int logId) {
        this.id = id;
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
        this.packetSize = packetSize;
        this.protocol = protocol;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.timestamp = timestamp;
        this.logId = logId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Integer getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Integer destinationPort) {
        this.destinationPort = destinationPort;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    @Override
    public String toString() {
        return "PacketEntity{id=" + id + ", sourceIp='" + sourceIp + "', destinationIp='" + destinationIp +
                "', packetSize=" + packetSize + ", protocol='" + protocol + "', sourcePort=" + sourcePort +
                ", destinationPort=" + destinationPort + ", timestamp=" + timestamp + ", logId=" + logId + "}";
    }
}
