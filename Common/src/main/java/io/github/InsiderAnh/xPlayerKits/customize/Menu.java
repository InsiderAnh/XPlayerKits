package io.github.InsiderAnh.xPlayerKits.customize;

import io.github.InsiderAnh.xPlayerKits.customize.actions.MenuAction;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.HashSet;

@Getter
public class Menu {

    private final String menuId;
    private final int rows;
    private final String title;
    private final HashMap<String, MenuItem> items = new HashMap<>();
    private final HashMap<String, MenuVarItem> varItems = new HashMap<>();

    private final HashSet<String> lastPageItems = new HashSet<>();
    private final HashSet<String> nextPageItems = new HashSet<>();

    public Menu(YamlConfiguration configuration, String menuId) {
        this.menuId = menuId;
        this.rows = configuration.getInt("rows");
        this.title = configuration.getString("title");

        if (configuration.isSet("items")) {
            for (String itemId : configuration.getConfigurationSection("items").getKeys(false)) {
                MenuItem menuItem = new MenuItem(configuration, itemId, "items." + itemId);
                items.put(itemId, menuItem);

                for (MenuAction action : menuItem.getActions()) {
                    if (action.getAction().equals("last_page")) {
                        lastPageItems.add(itemId);
                    }
                    if (action.getAction().equals("next_page")) {
                        nextPageItems.add(itemId);
                    }
                }
            }
        }
        if (configuration.isSet("varItems")) {
            for (String itemId : configuration.getConfigurationSection("varItems").getKeys(false)) {
                MenuVarItem menuVarItem = new MenuVarItem(configuration, "varItems." + itemId);
                varItems.put(itemId, menuVarItem);
            }
        }
    }

}