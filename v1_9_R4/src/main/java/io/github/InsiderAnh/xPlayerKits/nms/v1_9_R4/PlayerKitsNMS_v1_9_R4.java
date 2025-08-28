package io.github.InsiderAnh.xPlayerKits.nms.v1_9_R4;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.lang.reflect.Field;
import java.util.UUID;

public class PlayerKitsNMS_v1_9_R4 extends PlayerKitsNMS {

    @Override
    public void setUnbreakable(ItemMeta itemMeta, boolean unbreakable) {
        if (itemMeta == null) return;

        itemMeta.spigot().setUnbreakable(unbreakable);
    }

    @Override
    public boolean isUnbreakable(ItemMeta itemMeta) {
        return itemMeta.spigot().isUnbreakable();
    }

    @Override
    public int getCustomModelData(ItemMeta itemMeta) {
        return 0;
    }

    @Override
    public void setCustomModelData(ItemMeta itemMeta, int customModelData) {
    }

    @Override
    public void setGlowing(ItemMeta itemMeta, boolean glowing) {
        if (itemMeta == null) return;

        itemMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ItemStack value(ItemStack itemStack, String value) {
        SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", value));
        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException error) {
            error.printStackTrace();
        }
        itemStack.setItemMeta(headMeta);
        return itemStack;
    }

    @Override
    public ItemStack texture(ItemStack itemStack, String texture) {
        if (!texture.startsWith("http://textures.minecraft.net/texture/")) {
            texture = "http://textures.minecraft.net/texture/" + texture;
        }
        SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString(String.format("{textures:{SKIN:{url:\"%s\"}}}", texture))));
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException var8) {
            var8.printStackTrace();
        }
        itemStack.setItemMeta(headMeta);
        return itemStack;
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        IChatBaseComponent titleText = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
        IChatBaseComponent subtitleText = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");

        PacketPlayOutTitle packetTitleInfo = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);

        PacketPlayOutTitle packetTitleText = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleText);
        PacketPlayOutTitle packetSubtitleText = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleText);

        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        playerConnection.sendPacket(packetTitleInfo);
        playerConnection.sendPacket(packetSubtitleText);
        playerConnection.sendPacket(packetTitleText);
    }

    @Override
    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        try {
            location.getWorld().playSound(location, Sound.valueOf(sound.toUpperCase()), volume, pitch);
        } catch (NoSuchMethodError ignored) {
        }
    }

    @Override
    public void sendMiniMessage(Player player, String message) {
        player.sendMessage(message);
    }

}