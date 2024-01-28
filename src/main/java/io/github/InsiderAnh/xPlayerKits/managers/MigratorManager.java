package io.github.InsiderAnh.xPlayerKits.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MigratorManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    public void migrateKitsFromPlayerKits2(Player player) {
        //playerkits give Dios InsiderAnh
        File playerKitsDirectory = new File(playerKits.getServer().getWorldContainer(), "plugins/PlayerKits2/kits");
        if (!playerKitsDirectory.exists() || !playerKitsDirectory.isDirectory()) {
            playerKits.getLogger().info("You don´t have data yml in this plugin.");
            return;
        }
        for (File file : playerKitsDirectory.listFiles()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = file.getName().replace(".yml", "");
            Kit kit = new Kit(name);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playerkits give " + name + " " + player.getName());
            kit.setInventory(player.getInventory().getContents());
            kit.setArmor(player.getInventory().getArmorContents());
            kit.setOneTime(config.getBoolean("one_time"));
            kit.setAutoArmor(config.getBoolean("auto_armor"));
            kit.setCountdown(config.getInt("cooldown"));
            if (config.getBoolean("permission_required")) {
                kit.setPermission("playerkits.kit." + name);
            }
            kit.setPreview(true);
            kit.save();
            playerKits.getKitManager().addKit(kit);
        }
    }

    public void migrateFromPlayerKits2MySQL() {
        long startAt = System.currentTimeMillis();
        playerKits.getLogger().info("Starting migration from PlayerKits2.");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + playerKits.getConfig().getString("migrate.mysql.host") + ":" + playerKits.getConfig().getInt("migrate.mysql.port") + "/" + playerKits.getConfig().getString("migrate.mysql.database"));
        config.setUsername(playerKits.getConfig().getString("migrate.mysql.user"));
        config.setPassword(playerKits.getConfig().getString("migrate.mysql.password"));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("leakDetectionThreshold", "true");
        config.addDataSourceProperty("verifyServerCertificate", "false");
        config.addDataSourceProperty("useSSL", "false");
        config.setConnectionTimeout(5000);

        HikariDataSource dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT playerkits_players.UUID, playerkits_players.PLAYER_NAME, " +
                    "playerkits_players_kits.NAME, " +
                    "playerkits_players_kits.COOLDOWN, " +
                    "playerkits_players_kits.ONE_TIME, " +
                    "playerkits_players_kits.BOUGHT " +
                    "FROM playerkits_players LEFT JOIN playerkits_players_kits " +
                    "ON playerkits_players.UUID = playerkits_players_kits.UUID");

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String uuid = result.getString("UUID");
                String playerName = result.getString("PLAYER_NAME");
                String kitName = result.getString("NAME");
                long countdown = result.getLong("COOLDOWN");
                boolean oneTime = result.getBoolean("ONE_TIME");
                boolean bought = result.getBoolean("BOUGHT");

                PlayerKitData playerKitData = playerKits.getDatabase().getSyncPlayerData(UUID.fromString(uuid), playerName);
                playerKitData.getKitsData().put(kitName, new KitData(kitName, countdown, oneTime, bought));
                playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
            }

            playerKits.getLogger().info("Successfully migrated all player files in " + (System.currentTimeMillis() - startAt) + "ms.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void migrateFromPlayerKits2MoreOptimized() {
        long startAt = System.currentTimeMillis();
        playerKits.getLogger().info("Starting migration from PlayerKits2.");
        playerKits.getExecutor().execute(() -> {
            File playerKitsDirectory = new File(playerKits.getServer().getWorldContainer(), "plugins/PlayerKits2/players");
            if (!playerKitsDirectory.exists() || !playerKitsDirectory.isDirectory()) {
                playerKits.getLogger().info("You don´t have data yml in this plugin.");
                return;
            }
            AtomicInteger migratedPlayers = new AtomicInteger(), noHaveKitDataPlayers = new AtomicInteger();
            for (File file : playerKitsDirectory.listFiles()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String uuid = file.getName().replace(".yml", "");
                String name = config.getString("name");
                if (config.contains("kits")) {
                    PlayerKitData playerKitData = playerKits.getDatabase().getSyncPlayerData(UUID.fromString(uuid), name);
                    for (String key : config.getConfigurationSection("kits").getKeys(false)) {
                        long countdown = config.getLong("kits." + key + ".cooldown");
                        boolean oneTime = config.getBoolean("kits." + key + ".one_time");
                        boolean bought = config.getBoolean("kits." + key + ".bought");
                        playerKitData.getKitsData().put(key, new KitData(key, countdown, oneTime, bought));
                    }
                    playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
                    migratedPlayers.addAndGet(1);
                } else {
                    noHaveKitDataPlayers.addAndGet(1);
                }
                if (migratedPlayers.get() % 100 == 0) {
                    playerKits.getLogger().info("Migrated " + migratedPlayers.get() + " players correctly.");
                }
            }
            playerKits.getLogger().info("Successfully migrated " + migratedPlayers.get() + " player files and skipped " + noHaveKitDataPlayers.get() + " player files because they are unnecessary, lacking kit data in " + (System.currentTimeMillis() - startAt) + "ms.");
        });
    }


}