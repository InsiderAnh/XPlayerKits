package io.github.InsiderAnh.xPlayerKits.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.superclass.Database;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLDatabase extends Database {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private HikariDataSource dataSource;

    @Override
    public void connect() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + playerKits.getConfig().getString("databases.mysql.host") + ":" + playerKits.getConfig().getInt("databases.mysql.port") + "/" + playerKits.getConfig().getString("databases.mysql.database"));
            config.setUsername(playerKits.getConfig().getString("databases.mysql.user"));
            config.setPassword(playerKits.getConfig().getString("databases.mysql.password"));

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("autoReconnect", "true");
            config.addDataSourceProperty("leakDetectionThreshold", "true");
            config.addDataSourceProperty("verifyServerCertificate", "false");
            config.addDataSourceProperty("useSSL", "false");
            config.setConnectionTimeout(5000);

            dataSource = new HikariDataSource(config);

            try {
                Connection connection = getConnection();
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_kits (" +
                        "uuid VARCHAR(36) PRIMARY KEY," +
                        "name VARCHAR(36)," +
                        "data TEXT" +
                        ")");
                    close(null, statement, null);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE player_kits ADD COLUMN IF NOT EXISTS name VARCHAR(36);");
                    close(null, statement, null);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                close(connection, null, null);
            } catch (Exception exception) {
                exception.printStackTrace();
                playerKits.getLogger().warning("Error on connect to MySQL database.");
                return;
            }

            playerKits.getLogger().info("Connected to MySQL database correctly.");
        } catch (Exception exception) {
            exception.printStackTrace();
            playerKits.getLogger().warning("Error on connect to MySQL database.");
        }
    }

    @Override
    public void close() {
        dataSource.close();
    }

    @Override
    public CompletableFuture<PlayerKitData> getPlayerDataByName(String name) {
        CompletableFuture<PlayerKitData> completableFuture = new CompletableFuture<>();
        playerKits.getExecutor().execute(() -> {
            try {
                Connection connection = getConnection();
                PlayerKitData playerKitData;
                String data = getData(connection, "name", name);
                if (data != null) {
                    playerKitData = XPKUtils.getGson().fromJson(data, PlayerKitData.class);
                    completableFuture.complete(playerKitData);
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                completableFuture.complete(null);
            }
        });
        return completableFuture;
    }

    @Override
    public CompletableFuture<PlayerKitData> getPlayerData(UUID uuid, String name) {
        CompletableFuture<PlayerKitData> completableFuture = new CompletableFuture<>();
        playerKits.getExecutor().execute(() -> {
            try {
                completableFuture.complete(getSyncPlayerData(uuid, name));
            } catch (Exception exception) {
                exception.printStackTrace();
                completableFuture.complete(null);
            }
        });
        return completableFuture;
    }

    @Override
    public CompletableFuture<Boolean> loadPlayerData(UUID uuid, String name) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        playerKits.getExecutor().execute(() -> {
            try {
                Connection connection = getConnection();
                PlayerKitData playerKitData;
                boolean firstJoin;
                String data = getData(connection, "uuid", uuid.toString());
                if (data != null) {
                    firstJoin = false;
                    playerKitData = XPKUtils.getGson().fromJson(data, PlayerKitData.class);
                } else {
                    firstJoin = true;
                    playerKitData = new PlayerKitData(uuid, name);
                    insertData(connection, uuid.toString(), name, XPKUtils.getGson().toJson(playerKitData, PlayerKitData.class));
                }
                cachedPlayerKits.put(uuid, playerKitData);
                completableFuture.complete(firstJoin);
            } catch (Exception exception) {
                exception.printStackTrace();
                completableFuture.complete(null);
            }
        });
        return completableFuture;
    }

    @Override
    public PlayerKitData getSyncPlayerData(UUID uuid, String name) {
        Connection connection = getConnection();
        PlayerKitData playerKitData;
        String data = getData(connection, "uuid", uuid.toString());
        if (data != null) {
            playerKitData = XPKUtils.getGson().fromJson(data, PlayerKitData.class);
        } else {
            playerKitData = new PlayerKitData(uuid, name);
            insertData(connection, uuid.toString(), name, XPKUtils.getGson().toJson(playerKitData, PlayerKitData.class));
        }
        cachedPlayerKits.put(uuid, playerKitData);
        return playerKitData;
    }

    @Override
    public void updatePlayerData(UUID uuid) {
        PlayerKitData playerKitData = cachedPlayerKits.get(uuid);
        if (playerKitData == null) return;

        try {
            Connection connection = getConnection();
            updateData(connection, uuid.toString(), XPKUtils.getGson().toJson(playerKitData, PlayerKitData.class));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @SneakyThrows
    public Connection getConnection() {
        return dataSource.getConnection();
    }

    private void insertData(Connection connection, String uuid, String name, String data) {
        try {
            String insertSQL = "INSERT INTO player_kits (uuid, name, data) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setString(1, uuid);
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, data);
                preparedStatement.executeUpdate();
                close(connection, preparedStatement, null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String getData(Connection connection, String key, String uuid) {
        try {
            String selectSQL = "SELECT data FROM player_kits WHERE " + key + " = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
                preparedStatement.setString(1, uuid);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String data = resultSet.getString("data");
                        close(connection, preparedStatement, resultSet);
                        return data;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateData(Connection connection, String uuid, String updateData) {
        try {
            String updateSQL = "UPDATE player_kits SET data = ? WHERE uuid = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
                preparedStatement.setString(1, updateData);
                preparedStatement.setString(2, uuid);
                preparedStatement.executeUpdate();
                close(connection, preparedStatement, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(@Nullable Connection connection, @Nullable Statement preparedStatement, @Nullable ResultSet resultSet) {
        try {
            if (connection != null) {
                connection.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}