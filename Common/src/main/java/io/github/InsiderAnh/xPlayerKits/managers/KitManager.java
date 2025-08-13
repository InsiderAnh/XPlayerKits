package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;

@Getter
public class KitManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final HashMap<String, Kit> kits = new HashMap<>();
    private int lastPage = 1;

    public void load() {
        this.kits.clear();

        File kitsFolder = new File(playerKits.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
            playerKits.saveResource("kits/example_kit.yml", false);
        }
        for (File file : kitsFolder.listFiles()) {
            InsiderConfig config = new InsiderConfig(playerKits, "kits/" + file.getName().replace(".yml", ""), false, false);
            Kit kit = new Kit(config);
            kits.put(kit.getName().toLowerCase(), kit);
            playerKits.getLogger().info("Correctly loaded kit " + kit.getName() + ".");
        }
    }

    public Kit removeKit(String name) {
        return kits.remove(name.toLowerCase());
    }

    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public void addKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
        if (kit.getPage() > lastPage) {
            lastPage = kit.getPage();
        }
    }

}