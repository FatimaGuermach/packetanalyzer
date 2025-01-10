package model;

import java.time.LocalDateTime;

public class ThreatPacketEntity {
    private int id;
    private int threatId;
    private int packetId;

    public ThreatPacketEntity() {}

    public ThreatPacketEntity(int id, int threatId, int packetId) {
        this.id = id;
        this.threatId = threatId;
        this.packetId = packetId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getThreatId() {
        return threatId;
    }

    public void setThreatId(int threatId) {
        this.threatId = threatId;
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public String toString() {
        return "ThreatPacketEntity{id=" + id + ", threatId=" + threatId + ", packetId=" + packetId + "}";
    }
}
