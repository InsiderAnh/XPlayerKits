package io.github.InsiderAnh.xPlayerKits.nms.v1_13_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionPotionEffect;
import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.ChatMessageType;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerKitsNMS_v1_13_R2 extends PlayerKitsNMS  {

    @Override
    public void deserializePotionMeta(PotionMeta potionMeta, Map<String, Object> data) {
        if (data.containsKey("potion_data")) {
            String potionDataStr = (String) data.get("potion_data");
            String[] parts = potionDataStr.split(":");
            if (parts.length >= 3) {
                PotionType type = PotionType.valueOf(parts[0]);
                boolean extended = Boolean.parseBoolean(parts[1]);
                boolean upgraded = Boolean.parseBoolean(parts[2]);
                potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
            }
        }

        if (!data.containsKey("potion_effects")) return;

        List<?> effectList = (List<?>) data.get("potion_effects");
        for (Object effectObj : effectList) {
            PotionEffect effect = parsePotionEffect(effectObj.toString());
            if (effect != null) {
                potionMeta.addCustomEffect(effect, true);
            }
        }
    }

    private PotionEffect parsePotionEffect(String effectStr) {
        String[] parts = effectStr.split(":");
        if (parts.length < 3) return null;

        PotionEffectType type = CrossVersionPotionEffect.getEffect(parts[0]);
        if (type == null) return null;

        try {
            int amplifier = Integer.parseInt(parts[1]) - 1;
            int duration = Integer.parseInt(parts[2]);
            return new PotionEffect(type, duration, amplifier);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void setUnbreakable(ItemMeta itemMeta, boolean unbreakable) {
        if (itemMeta == null) return;

        itemMeta.setUnbreakable(unbreakable);
    }

    @Override
    public boolean isUnbreakable(ItemMeta itemMeta) {
        return itemMeta.isUnbreakable();
    }

    @Override
    public int getCustomModelData(ItemMeta itemMeta) {
        return 0;
    }

    @Override
    public void serializePotionMeta(ItemStack itemStack, YamlConfiguration config, String path) {
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        if (potionMeta == null) return;

        if (!potionMeta.hasCustomEffects()) return;

        List<String> effects = new ArrayList<>();
        for (PotionEffect effect : potionMeta.getCustomEffects()) {
            String effectId = CrossVersionPotionEffect.getEffectId(effect.getType());
            effects.add(effectId + ":" + (effect.getAmplifier() + 1) + ":" + effect.getDuration());
        }
        config.set(path + ".potion_effects", effects);
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
        try {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } catch (NoSuchMethodError error) {
            player.sendTitle(title, subtitle);
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), ChatMessageType.GAME_INFO);
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