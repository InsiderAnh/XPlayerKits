package io.github.InsiderAnh.xPlayerKits.api;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class PlayerKitsNMS {

    public abstract ItemStack value(ItemStack itemStack, String value);

    public abstract ItemStack texture(ItemStack itemStack, String texture);

    public abstract void setCustomModelData(ItemMeta itemMeta, int customModelData);

    public abstract void setGlowing(ItemMeta itemMeta, boolean glowing);

}