package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class MenuManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final HashMap<String, Menu> menus = new HashMap<>();

    public void load() {
        this.menus.clear();

        File menuFolder = new File(playerKits.getDataFolder(), "menus");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }

        checkFile(menuFolder, "preview");
        checkFile(menuFolder, "kits");

        playerKits.sendDebugMessage("Loading menus...");
        for (File file : menuFolder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            String menuId = file.getName().replace(".yml", "");

            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            this.menus.put(menuId, new Menu(configuration, menuId));

            playerKits.sendDebugMessage("Loaded menu " + menuId);
        }
    }

    void checkFile(File menuFolder, String menuId) {
        File file = new File(menuFolder, menuId + ".yml");
        if (!file.exists()) {
            playerKits.saveResource("menus/" + menuId + ".yml", false);
        }
    }

    @Nullable
    public Menu getMenu(String menuId) {
        return this.menus.get(menuId);
    }

}