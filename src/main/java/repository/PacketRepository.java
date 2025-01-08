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

    public List<PacketEntity> findTopDestinations(int logId) throws SQLException {
        String sql = """
                SELECT destination_ip, COUNT(*) AS packet_count
                FROM Packets
                WHERE log_id = ?
                GROUP BY destination_ip
                ORDER BY packet_count DESC
                LIMIT 10
                """;

        List<PacketEntity> topDestinations = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, logId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String destinationIp = resultSet.getString("destination_ip");
                    int packetCount = resultSet.getInt("packet_count");

                    // Créer une entité simplifiée pour représenter la destination et le compte
                    PacketEntity entity = new PacketEntity();
                    entity.setDestinationIp(destinationIp);
                    entity.setPacketSize(packetCount); // Utiliser le champ `packetSize` pour représenter le compte
                    topDestinations.add(entity);
                }
            }
        }
        return topDestinations;
    }

    public List<PacketEntity> findPacketsByLogId(int logId) throws SQLException {
        List<PacketEntity> packets = new ArrayList<>();
        String query = "SELECT id, source_ip, destination_ip, packet_size, protocol, source_port, destination_port, timestamp, log_id FROM packets WHERE log_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, logId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PacketEntity packet = new PacketEntity(
                            resultSet.getInt("id"),
                            resultSet.getString("source_ip"),
                            resultSet.getString("destination_ip"),
                            resultSet.getInt("packet_size"),
                            resultSet.getString("protocol"),
                            resultSet.getInt("source_port"),
                            resultSet.getInt("destination_port"),
                            resultSet.getTimestamp("timestamp").toLocalDateTime(),
                            resultSet.getInt("log_id")
                    );
                    packets.add(packet);
                }
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
