package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.customize.Menu;
import io.github.InsiderAnh.xPlayerKits.customize.MenuSlots;
import io.github.InsiderAnh.xPlayerKits.customize.MenuVarItem;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyInventory;
import lombok.Getter;

import java.io.File;
import java.util.*;

@Getter
public class KitManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final LinkedHashMap<String, Kit> kits = new LinkedHashMap<>();
    private final LinkedList<Integer> kitSlots = new LinkedList<>();
    private int lastPage = 1;

    public void load() {
        this.kits.clear();

        File kitsFolder = new File(playerKits.getDataFolder(), "kits");
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
            playerKits.saveResource("kits/example_kit.yml", false);
        }

        int perPage = 21;
        Menu menu = playerKits.getMenuManager().getMenu("kits");
        if (menu != null) {
            MenuVarItem menuVarItem = menu.getVarItems().get("kitSlots");
            if (menuVarItem != null) {
                MenuSlots menuSlots = menuVarItem.getSlots();
                perPage = menuSlots.getPerPage();
                this.kitSlots.addAll(menuSlots.getSlots());
            }
        }

        List<Kit> loadedKits = new ArrayList<>();
        Map<String, Kit> forcedPositionKits = new HashMap<>();
        List<Kit> flexibleKits = new ArrayList<>();

        for (File file : kitsFolder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;

            Kit kit;
            try {
                InsiderConfig config = new InsiderConfig(playerKits, "kits/" + file.getName().replace(".yml", ""), false, false);
                kit = new Kit(config);
            } catch (Exception e) {
                playerKits.getLogger().info("Error loading kit " + file.getName() + ": " + e.getMessage());
                continue;
            }
            if (kit.getPropertyInventory() == null || kit.getPropertyTiming() == null) {
                playerKits.getLogger().info("Error loading kit " + file.getName() + ": null");
                continue;
            }

            loadedKits.add(kit);
            playerKits.getLogger().info("Correctly loaded kit " + kit.getName() + ".");
        }

        for (Kit kit : loadedKits) {
            PropertyInventory property = kit.getPropertyInventory();
            if (property.getPage() != -1 && property.getSlot() != -1) {
                String key = property.getPage() + "-" + property.getSlot();
                forcedPositionKits.put(key, kit);
                if (property.getPage() > lastPage) {
                    lastPage = property.getPage();
                }
            } else {
                flexibleKits.add(kit);
            }
        }

        organizeKits(forcedPositionKits, flexibleKits, perPage);
    }

    public Kit removeKit(String name) {
        return kits.remove(name.toLowerCase());
    }

    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public void addKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
        if (kit.getPropertyInventory().getPage() > lastPage) {
            lastPage = kit.getPropertyInventory().getPage();
        }
        reorganizeKitsAfterAdd();
    }

    private void reorganizeKitsAfterAdd() {
        Map<String, Kit> forcedPositionKits = new HashMap<>();
        List<Kit> flexibleKits = new ArrayList<>();

        int perPage = 21;
        Menu menu = playerKits.getMenuManager().getMenu("kits");
        if (menu != null) {
            MenuVarItem menuVarItem = menu.getVarItems().get("kitSlots");
            if (menuVarItem != null) {
                MenuSlots menuSlots = menuVarItem.getSlots();
                perPage = menuSlots.getPerPage();
            }
        }

        for (Kit kit : new ArrayList<>(kits.values())) {
            if (kit == null || kit.getPropertyInventory() == null || kit.getPropertyTiming() == null) continue;

            PropertyInventory property = kit.getPropertyInventory();
            if (property.getPage() != -1 && property.getSlot() != -1) {
                String key = property.getPage() + "-" + property.getSlot();
                forcedPositionKits.put(key, kit);
            } else {
                flexibleKits.add(kit);
            }
        }

        kits.clear();
        organizeKits(forcedPositionKits, flexibleKits, perPage);
    }

    private void organizeKits(Map<String, Kit> forcedPositionKits, List<Kit> flexibleKits, int perPage) {
        int totalKits = forcedPositionKits.size() + flexibleKits.size();
        int maxPagesNeeded = (int) Math.ceil((double) totalKits / perPage);

        for (Kit kit : forcedPositionKits.values()) {
            if (kit == null || kit.getPropertyInventory() == null || kit.getPropertyTiming() == null) continue;
            if (kit.getPropertyInventory().getPage() > maxPagesNeeded) {
                maxPagesNeeded = kit.getPropertyInventory().getPage();
            }
        }

        this.lastPage = Math.max(this.lastPage, maxPagesNeeded);

        Map<String, Kit> finalStructure = new LinkedHashMap<>();
        Iterator<Kit> flexibleIterator = flexibleKits.iterator();

        for (int page = 1; page <= lastPage; page++) {
            for (int slot = 0; slot < perPage; slot++) {
                String positionKey = page + "-" + slot;
                if (forcedPositionKits.containsKey(positionKey)) {
                    Kit forcedKit = forcedPositionKits.get(positionKey);
                    finalStructure.put(forcedKit.getName().toLowerCase(), forcedKit);
                } else if (flexibleIterator.hasNext()) {
                    Kit flexibleKit = flexibleIterator.next();
                    int kitSlot = kitSlots.size() <= slot ? 0 : kitSlots.get(slot);
                    flexibleKit.getPropertyInventory().setPage(page);
                    flexibleKit.getPropertyInventory().setSlot(kitSlot);
                    finalStructure.put(flexibleKit.getName().toLowerCase(), flexibleKit);
                } else {
                    finalStructure.put(UUID.randomUUID().toString(), null);
                }
            }
        }

        this.kits.putAll(finalStructure);
    }

    public Map<String, Kit> getSubMap(int skip, int limit) {
        if (skip < 0) skip = 0;
        if (limit < skip) limit = skip;
        if (skip >= kits.size()) return new LinkedHashMap<>();

        int actualLimit = Math.min(limit, kits.size());

        Map<String, Kit> result = new LinkedHashMap<>();
        kits.entrySet().stream()
            .skip(skip)
            .limit((long) actualLimit - skip)
            .forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        return result;
    }

}