package hexlet.code.repository;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlsRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        url.setCreatedAt(createdAt);
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, url.getName());
            stmt.setTimestamp(2, url.getCreatedAt());
            stmt.executeUpdate();

            var generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static Optional<Url> find(Long id) throws SQLException {
        var sql = "SELECT * FROM urls WHERE id = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var urlChecks = UrlsCheckRepository.getEntities(id).orElse(new ArrayList<>());
                var url = Url.builder()
                        .id(id)
                        .name(name)
                        .createdAt(createdAt)
                        .urlChecks(urlChecks)
                        .build();
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static List<Url> getEntities() throws SQLException {
        var sql = "SELECT * FROM urls";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var result = new ArrayList<Url>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                List<UrlCheck> urlChecks = UrlsCheckRepository.getEntities(id).orElse(new ArrayList<>());
                var url = Url.builder()
                        .id(id)
                        .name(name)
                        .createdAt(createdAt)
                        .urlChecks(urlChecks)
                        .build();
                result.add(url);
            }
            return result;
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        var sql = "SELECT * FROM urls WHERE name = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                Long id = resultSet.getLong("id");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                List<UrlCheck> urlChecks = UrlsCheckRepository.getEntities(id).orElse(new ArrayList<>());
                Url url = Url.builder()
                        .id(id)
                        .name(name)
                        .createdAt(createdAt)
                        .urlChecks(urlChecks)
                        .build();
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }
}
