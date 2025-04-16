package io.github.InsiderAnh.xPlayerKits.database;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.superclass.Database;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteDatabase extends Database {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private Connection connection;

    @Override
    public void connect() {
        try {
            File dbFile = new File(playerKits.getDataFolder(), playerKits.getConfig().getString("databases.h2.database") + ".db");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }

            Class.forName("org.sqlite.JDBC");
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            } catch (SQLException e) {
                e.printStackTrace();
            }

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
                close(connection, null, null);
            } catch (Exception exception) {
                exception.printStackTrace();
                playerKits.getLogger().warning("Error on connect to SQLite database.");
                return;
            }

            playerKits.getLogger().info("Connected to SQLite database correctly.");
        } catch (Exception exception) {
            exception.printStackTrace();
            playerKits.getLogger().warning("Error on connect to SQLite database.");
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        return connection;
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