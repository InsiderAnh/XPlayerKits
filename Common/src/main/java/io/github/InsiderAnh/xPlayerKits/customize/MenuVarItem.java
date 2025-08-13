package io.github.InsiderAnh.xPlayerKits.customize;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.LanguageUtils;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

@Getter
public class MenuVarItem {

    private final String displayName;
    private final List<String> lore;
    private final MenuSlots slots;

    public MenuVarItem(YamlConfiguration configuration, String path) {
        this.displayName = PlayerKits.getInstance().getColorUtils().color(configuration.getString(path + ".display-name", ""));
        this.lore = PlayerKits.getInstance().getColorUtils().color(configuration.isSet(path + ".lore") ? configuration.getStringList(path + ".lore") : Collections.emptyList());
        this.slots = new MenuSlots(configuration, path + ".slots");
    }

    @Override
    public String toString() {
        return "MenuVarItem{" +
            "displayName='" + displayName + '\'' +
            ", lore=" + lore +
            ", slots=" + slots +
            '}';
    }

    public ItemStack buildItem(Player player, ItemStack itemStack, Placeholder... placeholders) {
        ItemStack varItem = itemStack.clone();
        ItemMeta itemMeta = varItem.getItemMeta();
        itemMeta.setDisplayName(LanguageUtils.replacePlaceholders(displayName, placeholders));
        itemMeta.setLore(LanguageUtils.replacePlaceholders(lore, placeholders));
        varItem.setItemMeta(itemMeta);
        return varItem;
    }

}