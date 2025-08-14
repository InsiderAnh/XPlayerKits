package io.github.InsiderAnh.xPlayerKits.api;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class PlayerKitsNMS {

    public abstract ItemStack value(ItemStack itemStack, String value);

    public abstract ItemStack texture(ItemStack itemStack, String texture);

    public abstract void setCustomModelData(ItemMeta itemMeta, int customModelData);

    public abstract void setUnbreakable(ItemMeta itemMeta, boolean unbreakable);

    public abstract void setGlowing(ItemMeta itemMeta, boolean glowing);

    public abstract boolean isUnbreakable(ItemMeta itemMeta);

    public abstract int getCustomModelData(ItemMeta itemMeta);

}