package repository;

import model.ThreatEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThreatRepository {
    public void createThreat(ThreatEntity threat) throws SQLException {
        String sql = "INSERT INTO Threats (threat_level) VALUES (?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, threat.getThreatLevel());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    threat.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<ThreatEntity> findAll() throws SQLException {
        String sql = "SELECT * FROM Threats";
        List<ThreatEntity> threats = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                threats.add(mapToThreatEntity(resultSet));
            }
        }
        return threats;
    }

    private ThreatEntity mapToThreatEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String threatLevel = resultSet.getString("threat_level");
        return new ThreatEntity(id, threatLevel);
    }
}
