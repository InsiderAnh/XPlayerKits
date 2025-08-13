package io.github.InsiderAnh.xPlayerKits.customize;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.api.ColorUtils;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.LanguageUtils;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

@Getter
public class MenuItem {

    private final PlayerKits plugin = PlayerKits.getInstance();
    private final String material;
    private final int amount;
    private final int customModelData;
    private final boolean glowing;
    private final String displayName;
    private final List<String> lore;
    private final MenuSlots slots;

    public MenuItem(YamlConfiguration configuration, String path) {
        this.material = configuration.getString(path + ".material");
        this.amount = configuration.getInt(path + ".amount");
        this.customModelData = configuration.getInt(path + ".custom-model-data");
        this.glowing = configuration.getBoolean(path + ".glowing");
        this.displayName = configuration.getString(path + ".display-name", "");
        this.lore = configuration.isList(path + ".lore") ? configuration.getStringList(path + ".lore") : Collections.emptyList();
        this.slots = new MenuSlots(configuration, path + ".slots");
    }

    public ItemStack buildItem(Player player, Placeholder... placeholders) {
        ColorUtils colorUtils = PlayerKits.getInstance().getColorUtils();
        ItemStack itemStack = XPKUtils.parseItemStack(player, material);
        if (amount > 0) {
            itemStack.setAmount(amount);
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (customModelData != 0) {
            plugin.getPlayerKitsNMS().setCustomModelData(itemMeta, customModelData);
        }

        if (glowing) {
            plugin.getPlayerKitsNMS().setGlowing(itemMeta, true);
        }

        if (displayName != null) {
            itemMeta.setDisplayName(LanguageUtils.replacePlaceholders(colorUtils.color(displayName), placeholders));
        }
        if (!lore.isEmpty()) {
            itemMeta.setLore(LanguageUtils.replacePlaceholders(colorUtils.color(lore), placeholders));
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }


}