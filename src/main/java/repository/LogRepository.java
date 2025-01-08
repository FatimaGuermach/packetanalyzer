package repository;

import model.LogEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogRepository {
    public void createLog(LogEntity log) throws SQLException {
        String sql = "INSERT INTO Logs (launch_time) VALUES (?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setTimestamp(1, Timestamp.valueOf(log.getLaunchTime()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    log.setId(keys.getInt(1));
                }
            }
        }
    }

    public List<LogEntity> findAll() throws SQLException {
        String sql = "SELECT * FROM Logs";
        List<LogEntity> logs = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                logs.add(mapToLogEntity(resultSet));
            }
        }
        return logs;
    }

    public int getCurrentLogId() throws SQLException {
        String query = "SELECT id FROM Logs ORDER BY launch_time DESC LIMIT 1";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("No logs found in the database.");
            }
        }
    }

    private LogEntity mapToLogEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        LocalDateTime launchTime = resultSet.getTimestamp("launch_time").toLocalDateTime();
        return new LogEntity(id, launchTime);
    }
}
