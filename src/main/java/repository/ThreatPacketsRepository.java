package repository;

import model.ThreatPacketEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThreatPacketsRepository {

    public void createThreatPacket(ThreatPacketEntity threatPacket) throws SQLException {
        String sql = "INSERT INTO Threat_Packets (threat_id, packet_id) VALUES (?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, threatPacket.getThreatId());
            statement.setInt(2, threatPacket.getPacketId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    threatPacket.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<ThreatPacketEntity> findAll() throws SQLException {
        String sql = "SELECT * FROM Threat_Packets";
        List<ThreatPacketEntity> threatPackets = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                threatPackets.add(mapToThreatPacketEntity(resultSet));
            }
        }
        return threatPackets;
    }

    private ThreatPacketEntity mapToThreatPacketEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int threatId = resultSet.getInt("threat_id");
        int packetId = resultSet.getInt("packet_id");
        return new ThreatPacketEntity(id, threatId, packetId);
    }
}
