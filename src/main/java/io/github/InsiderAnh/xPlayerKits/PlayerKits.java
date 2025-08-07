package io.github.InsiderAnh.xPlayerKits;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import com.cjcrafter.foliascheduler.util.ServerVersions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.github.InsiderAnh.xPlayerKits.bstats.MetricsLite;
import io.github.InsiderAnh.xPlayerKits.commands.XKitsCommands;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.database.MySQLDatabase;
import io.github.InsiderAnh.xPlayerKits.database.SQLiteDatabase;
import io.github.InsiderAnh.xPlayerKits.hooks.StellarTaskHook;
import io.github.InsiderAnh.xPlayerKits.hooks.tasks.BukkitTaskHook;
import io.github.InsiderAnh.xPlayerKits.hooks.tasks.FoliaTaskHook;
import io.github.InsiderAnh.xPlayerKits.listeners.PlayerListener;
import io.github.InsiderAnh.xPlayerKits.managers.ConfigManager;
import io.github.InsiderAnh.xPlayerKits.managers.KitManager;
import io.github.InsiderAnh.xPlayerKits.placeholders.PlayerKitsPlaceholders;
import io.github.InsiderAnh.xPlayerKits.superclass.Database;
import io.github.InsiderAnh.xPlayerKits.utils.UpdateChecker;
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
    private InsiderConfig lang;
    private InsiderConfig inventories;
    private Database database;
    private MetricsLite bstats;
    private UpdateChecker updateChecker;

    public PlayerKits() {
        instance = this;
        this.executor = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1024)));
        this.kitManager = new KitManager();
        this.configManager = new ConfigManager();
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

        bstats = new MetricsLite(this, 26821);
        updateChecker = new UpdateChecker();
    }

    @Override
    public void onDisable() {
        database.close();
        bstats.shutdown();
    }

    public void reload() {
        boolean lasEnabled = getConfig().getBoolean("kitsCMD.enabled");

        this.reloadConfig();
        this.lang.reload();
        this.inventories.reload();
        this.configManager.load();
        this.kitManager.load();

        if (!lasEnabled && getConfig().getBoolean("kitsCMD.enabled")) {
            getCommand("kits").setExecutor(new XKitsCommands());
        }
    }

    public void sendDebugMessage(String... messages) {
        for (String message : messages) {
            getLogger().info("§6[PlayerKits] §f" + message);
        }
    }

    public StellarTaskHook getStellarTaskHook(Runnable runnable) {
        if (MinecraftVersions.WILD_UPDATE.isAtLeast() && ServerVersions.isFolia()) {
            return new FoliaTaskHook(runnable);
        }
        return new BukkitTaskHook(runnable);
    }

}