package services;

import model.PacketEntity;
import model.ThreatEntity;
import repository.PacketRepository;
import repository.ThreatRepository;

import java.sql.SQLException;
import java.util.List;

public class TrafficAnalysisService {
    private final PacketRepository packetRepository;
    private final ThreatRepository threatRepository;

    public TrafficAnalysisService(PacketRepository packetRepository, ThreatRepository threatRepository) {
        this.packetRepository = packetRepository;
        this.threatRepository = threatRepository;
    }

    public void analyzeTraffic() {
        List<PacketEntity> packets = null;
        try {
            packets = packetRepository.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (PacketEntity packet : packets) {
            if (packet.getPacketSize() > 1000) {
                ThreatEntity threat = new ThreatEntity(0, "High");
                try {
                    threatRepository.createThreat(threat);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
