package io.github.InsiderAnh.xPlayerKits;

import com.cjcrafter.foliascheduler.util.MinecraftVersions;
import com.cjcrafter.foliascheduler.util.ServerVersions;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.github.InsiderAnh.xPlayerKits.api.ColorUtils;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import io.github.InsiderAnh.xPlayerKits.bstats.MetricsLite;
import io.github.InsiderAnh.xPlayerKits.commands.XKitsCommands;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.database.MySQLDatabase;
import io.github.InsiderAnh.xPlayerKits.database.SQLiteDatabase;
import io.github.InsiderAnh.xPlayerKits.enums.MinecraftVersion;
import io.github.InsiderAnh.xPlayerKits.hooks.StellarTaskHook;
import io.github.InsiderAnh.xPlayerKits.hooks.tasks.BukkitTaskHook;
import io.github.InsiderAnh.xPlayerKits.hooks.tasks.FoliaTaskHook;
import io.github.InsiderAnh.xPlayerKits.listeners.PlayerListener;
import io.github.InsiderAnh.xPlayerKits.managers.ConfigManager;
import io.github.InsiderAnh.xPlayerKits.managers.KitManager;
import io.github.InsiderAnh.xPlayerKits.managers.MenuManager;
import io.github.InsiderAnh.xPlayerKits.placeholders.PlayerKitsPlaceholders;
import io.github.InsiderAnh.xPlayerKits.superclass.Database;
import io.github.InsiderAnh.xPlayerKits.utils.UpdateChecker;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
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
    private final MenuManager menuManager;
    private InsiderConfig lang;
    private Database database;
    private PlayerKitsNMS playerKitsNMS;
    private ColorUtils colorUtils;
    private MetricsLite bstats;
    private String version;
    private MinecraftVersion localVersion;
    private String completer;
    private UpdateChecker updateChecker;

    public PlayerKits() {
        instance = this;
        this.executor = MoreExecutors.listeningDecorator(new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1024)));
        this.kitManager = new KitManager();
        this.configManager = new ConfigManager();
        this.menuManager = new MenuManager();
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.lang = new InsiderConfig(this, "lang", true, false);

        String databaseType = getConfig().getString("databases.databaseType", "h2");
        if (databaseType.equalsIgnoreCase("mysql")) {
            this.database = new MySQLDatabase();
        } else {
            this.database = new SQLiteDatabase();
        }
        this.database.connect();
        this.configManager.load();
        this.kitManager.load();
        this.menuManager.load();

        getCommand("xkits").setExecutor(new XKitsCommands());
        if (getConfig().getBoolean("kitsCMD.enabled")) {
            XPKUtils.registerCommandDynamic(new XKitsCommands());
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
        this.configManager.load();
        this.kitManager.load();
        this.menuManager.load();

        if (!lasEnabled && getConfig().getBoolean("kitsCMD.enabled")) {
            XPKUtils.registerCommandDynamic(new XKitsCommands());
        }
    }

    public void sendDebugMessage(String... messages) {
        for (String message : messages) {
            getLogger().info("§6[xPlayerKits] §f" + message);
        }
    }

    public StellarTaskHook getStellarTaskHook(Runnable runnable) {
        if (MinecraftVersions.WILD_UPDATE.isAtLeast() && ServerVersions.isFolia()) {
            return new FoliaTaskHook(runnable);
        }
        return new BukkitTaskHook(runnable);
    }

    public PlayerKitsNMS getPlayerKitsNMS() {
        if (playerKitsNMS == null) {
            loadNMS();
        }
        return playerKitsNMS;
    }

    public ColorUtils getColorUtils() {
        if (colorUtils == null) {
            loadNMS();
        }
        return colorUtils;
    }

    @SneakyThrows
    public void loadNMS() {
        String cbPackage = Bukkit.getServer().getClass().getPackage().getName();
        String detectedVersion = cbPackage.substring(cbPackage.lastIndexOf('.') + 1);
        if (!detectedVersion.startsWith("v")) {
            detectedVersion = Bukkit.getServer().getBukkitVersion();
        }

        version = detectedVersion;

        getLogger().info("Detected Minecraft version: " + version);

        localVersion = MinecraftVersion.get(version);
        if (localVersion == null) {
            Bukkit.getLogger().warning("[XPlayerKits] No found Minecraft version " + version + ". If you want to support this version, contact InsiderAnh.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (localVersion.equals(MinecraftVersion.v1_8)) {
            this.completer = "v1_8_R3";
        } else if (localVersion.equals(MinecraftVersion.v1_9)) {
            this.completer = "v1_9_R4";
        } else if (localVersion.equals(MinecraftVersion.v1_12)) {
            this.completer = "v1_12_R2";
        } else if (localVersion.equals(MinecraftVersion.v1_13)) {
            this.completer = "v1_13_R2";
        } else if (localVersion.equals(MinecraftVersion.v1_16)) {
            this.completer = "v1_16_R5";
        } else if (localVersion.equals(MinecraftVersion.v1_17)) {
            this.completer = "v1_17_R1";
        } else {
            this.completer = localVersion.name();
        }

        getLogger().info("Loaded " + completer + " version.");

        this.playerKitsNMS = Class.forName("io.github.InsiderAnh.xPlayerKits.nms." + completer + ".PlayerKitsNMS_" + completer).asSubclass(PlayerKitsNMS.class).getConstructor().newInstance();
        this.colorUtils = Class.forName("io.github.InsiderAnh.xPlayerKits.nms." + completer + ".ColorUtils_" + completer).asSubclass(ColorUtils.class).getConstructor().newInstance();

    }

}