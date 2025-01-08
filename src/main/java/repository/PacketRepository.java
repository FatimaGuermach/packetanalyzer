package repository;

import model.PacketEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PacketRepository {
    public void createPacket(PacketEntity packet) throws SQLException {
        String sql = "INSERT INTO Packets (source_ip, destination_ip, packet_size, protocol, source_port, " +
                "destination_port, timestamp, log_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, packet.getSourceIp());
            statement.setString(2, packet.getDestinationIp());
            statement.setInt(3, packet.getPacketSize());
            statement.setString(4, packet.getProtocol());
            statement.setObject(5, packet.getSourcePort(), Types.INTEGER);
            statement.setObject(6, packet.getDestinationPort(), Types.INTEGER);
            statement.setTimestamp(7, Timestamp.valueOf(packet.getTimestamp()));
            statement.setInt(8, packet.getLogId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    packet.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<PacketEntity> findAll() throws SQLException {
        String sql = "SELECT * FROM Packets";
        List<PacketEntity> packets = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                packets.add(mapToPacketEntity(resultSet));
            }
        }
        return packets;
    }

    private PacketEntity mapToPacketEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String sourceIp = resultSet.getString("source_ip");
        String destinationIp = resultSet.getString("destination_ip");
        int packetSize = resultSet.getInt("packet_size");
        String protocol = resultSet.getString("protocol");
        Integer sourcePort = resultSet.getObject("source_port", Integer.class);
        Integer destinationPort = resultSet.getObject("destination_port", Integer.class);
        LocalDateTime timestamp = resultSet.getTimestamp("timestamp").toLocalDateTime();
        int logId = resultSet.getInt("log_id");
        return new PacketEntity(id, sourceIp, destinationIp, packetSize, protocol, sourcePort, destinationPort, timestamp, logId);
    }
}
