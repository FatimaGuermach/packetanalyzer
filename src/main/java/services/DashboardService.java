package services;

import model.PacketEntity;
import repository.PacketRepository;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {
    private final PacketRepository packetRepository;

    public DashboardService(PacketRepository packetRepository) {
        this.packetRepository = packetRepository;
    }

    public List<PacketEntity> getTopPackets() {
        try {
            return packetRepository.findAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
