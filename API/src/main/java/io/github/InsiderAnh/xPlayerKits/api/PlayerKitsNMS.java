package io.github.InsiderAnh.xPlayerKits.api;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.Map;

public abstract class PlayerKitsNMS {

    public abstract void sendMiniMessage(Player player, String message);

    public abstract void sendActionBar(Player player, String message);

    public abstract void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    public abstract void playSound(Location location, String sound, float volume, float pitch);

    public abstract ItemStack value(ItemStack itemStack, String value);

    public abstract ItemStack texture(ItemStack itemStack, String texture);

    public abstract void setCustomModelData(ItemMeta itemMeta, int customModelData);

    public abstract void setUnbreakable(ItemMeta itemMeta, boolean unbreakable);

    public abstract void setGlowing(ItemMeta itemMeta, boolean glowing);

    public abstract boolean isUnbreakable(ItemMeta itemMeta);

    public abstract int getCustomModelData(ItemMeta itemMeta);

    public abstract void deserializePotionMeta(PotionMeta potionMeta, Map<String, Object> data);

    public abstract void serializePotionMeta(ItemStack itemStack, YamlConfiguration config, String path);

}