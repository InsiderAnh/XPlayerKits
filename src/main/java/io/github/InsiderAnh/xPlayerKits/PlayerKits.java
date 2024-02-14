package io.github.InsiderAnh.xPlayerKits;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.github.InsiderAnh.xPlayerKits.commands.XKitsCommands;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.database.MongoDatabase;
import io.github.InsiderAnh.xPlayerKits.database.MySQLDatabase;
import io.github.InsiderAnh.xPlayerKits.database.SQLiteDatabase;
import io.github.InsiderAnh.xPlayerKits.listeners.PlayerListener;
import io.github.InsiderAnh.xPlayerKits.managers.ConfigManager;
import io.github.InsiderAnh.xPlayerKits.managers.KitManager;
import io.github.InsiderAnh.xPlayerKits.placeholders.PlayerKitsPlaceholders;
import io.github.InsiderAnh.xPlayerKits.superclass.Database;
import io.github.InsiderAnh.xPlayerKits.utils.NBTEditor;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public class PlayerKits extends JavaPlugin {

    @Getter
    private static PlayerKits instance;
    private final ListeningExecutorService executor;
    private final KitManager kitManager;
    private final ConfigManager configManager;
    private final NBTEditor nbtEditor;
    private InsiderConfig lang, inventories;
    private Database database;

    public PlayerKits() {
        instance = this;
        this.executor = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1024)));
        this.kitManager = new KitManager();
        this.configManager = new ConfigManager();
        this.nbtEditor = new NBTEditor();
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.lang = new InsiderConfig(this, "lang", true, false);
        this.inventories = new InsiderConfig(this, "inventories", true, false);

        String databaseType = getConfig().getString("databases.databaseType", "h2");
        if (databaseType.equalsIgnoreCase("mysql")) {
            this.database = new MySQLDatabase();
        } else if (databaseType.equalsIgnoreCase("mongodb")) {
            this.database = new MongoDatabase();
        } else {
            this.database = new SQLiteDatabase();
        }
        this.database.connect();
        this.configManager.load();
        this.kitManager.load();

        getCommand("xkits").setExecutor(new XKitsCommands());
        if (getConfig().getBoolean("kitsCMD.enabled")) {
            getCommand("kits").setExecutor(new XKitsCommands());
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlayerKitsPlaceholders().register();
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        database.close();
    }

    public void reload() {
        this.reloadConfig();
        this.lang.reload();
        this.inventories.reload();
        this.configManager.load();
        this.kitManager.load();
    }

}