package io.github.InsiderAnh.xPlayerKits.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtils {

    public final ItemMeta itemMeta;
    public ItemStack item;

    public ItemUtils(@Nullable Material material) {
        this(material, 1);
    }

    public ItemUtils(@Nullable Material material, int amount) {
        this.item = new ItemStack(material == null ? Material.STONE : material, amount);
        this.itemMeta = item.getItemMeta();
    }

    public ItemUtils(@Nullable ItemStack item) {
        this.item = item == null ? new ItemStack(Material.STONE) : item.clone();
        this.itemMeta = this.item.getItemMeta();
    }

    public ItemUtils type(Material material) {
        item.setType(material);
        return this;
    }

    public ItemUtils data(short data) {
        this.item = new ItemStack(item.getType(), item.getAmount(), data);
        return this;
    }

    public ItemUtils displayName(String displayName) {
        itemMeta.setDisplayName(XPKUtils.color(displayName));
        item.setItemMeta(itemMeta);
        return this;
    }

    public int enchantLevel(Enchantment enchantment) {
        return item.getEnchantments().getOrDefault(enchantment, 0);
    }

    public ItemUtils unEnchant(Enchantment enchantment) {
        itemMeta.removeEnchant(enchantment);
        return this;
    }

    public ItemUtils durability(int damage) {
        item.setDurability((short) Math.max(item.getType().getMaxDurability() - Math.max(damage, 1), 0));
        return this;
    }

    public int durability() {
        return item.getType().getMaxDurability() - item.getDurability();
    }

    public int maxDurability() {
        return item.getType().getMaxDurability();
    }

    public ItemUtils amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public int amount() {
        return item.getAmount();
    }

    public ItemUtils lore(String lore) {
        return lore(null, XPKUtils.color(lore), false);
    }

    public ItemUtils lore(Player p, String lore, boolean placeholders) {
        if (placeholders) {
            itemMeta.setLore(lore.isEmpty() ? new ArrayList<>() : Arrays.asList(PlaceholderAPI.setPlaceholders(p, lore).split("\\n")));
        } else {
            itemMeta.setLore(lore.isEmpty() ? new ArrayList<>() : Arrays.asList(lore.split("\\n")));
        }
        return this;
    }

    public ItemUtils setLoreList(List<String> lore) {
        itemMeta.setLore(lore);
        return this;
    }

    public ItemUtils owner(String owner) {
        if (!item.getType().name().contains("SKULL_ITEM") && !item.getType().name().contains("PLAYER_HEAD"))
            return this;
        if (owner.isEmpty()) return this;
        SkullMeta headMeta = (SkullMeta) itemMeta;
        headMeta.setOwner(owner);
        return this;
    }

    public ItemUtils item(ItemStack item) {
        this.item = item;
        return this;
    }

    public ItemUtils glowing() {
        itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemUtils unbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemUtils enchant(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemUtils hideAttributes() {
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(itemMeta);
        return item;
    }

}