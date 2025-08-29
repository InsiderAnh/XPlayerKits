package io.github.InsiderAnh.xPlayerKits.nms.v1_20_R4;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionPotionEffect;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class PlayerKitsNMS_v1_20_R4 extends PlayerKitsNMS {

    private final Gson GSON = new Gson();
    private final Map<String, String> replacements = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerKitsNMS_v1_20_R4() {
        replacements.put("0", "<black>");
        replacements.put("1", "<dark_blue>");
        replacements.put("2", "<dark_green>");
        replacements.put("3", "<dark_aqua>");
        replacements.put("4", "<dark_red>");
        replacements.put("5", "<dark_purple>");
        replacements.put("6", "<gold>");
        replacements.put("7", "<gray>");
        replacements.put("8", "<dark_gray>");
        replacements.put("9", "<blue>");
        replacements.put("a", "<green>");
        replacements.put("b", "<aqua>");
        replacements.put("c", "<red>");
        replacements.put("d", "<light_purple>");
        replacements.put("e", "<yellow>");
        replacements.put("f", "<white>");
        replacements.put("k", "<obfuscated>");
        replacements.put("l", "<bold>");
        replacements.put("m", "<strikethrough>");
        replacements.put("n", "<underlined>");
        replacements.put("o", "<italic>");
        replacements.put("r", "<reset>");
    }

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
    public void serializePotionMeta(ItemStack itemStack, YamlConfiguration config, String path) {
        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        if (potionMeta == null) return;

        PotionData potionData = potionMeta.getBasePotionData();
        if (potionData != null) {
            config.set(path + ".potion_data", potionData.getType().name().toLowerCase() + ":" + potionData.isExtended() + ":" + potionData.isUpgraded());
        }

        if (!potionMeta.hasCustomEffects()) return;

        List<String> effects = new ArrayList<>();
        for (PotionEffect effect : potionMeta.getCustomEffects()) {
            String effectId = CrossVersionPotionEffect.getEffectId(effect.getType());
            effects.add(effectId + ":" + (effect.getAmplifier() + 1) + ":" + effect.getDuration());
        }
        config.set(path + ".potion_effects", effects);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    @Override
    public void sendActionBar(Player player, String message) {
        player.sendActionBar(Component.text(message));
    }

    @Override
    public void sendMiniMessage(Player player, String message) {
        player.sendMessage(miniMessage.deserialize(replaceColors(PlaceholderAPI.setPlaceholders(player, message))));
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
        return itemMeta.getCustomModelData();
    }

    @Override
    public void setCustomModelData(ItemMeta itemMeta, int customModelData) {
        if (itemMeta == null) return;

        itemMeta.setCustomModelData(customModelData);
    }

    @Override
    public void setGlowing(ItemMeta itemMeta, boolean glowing) {
        if (itemMeta == null) return;

        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    @Override
    public ItemStack value(ItemStack itemStack, String value) {
        if (value.isEmpty()) return itemStack;
        if (value.startsWith("https://textures.minecraft.net/texture/")) {
            return texture(itemStack, value);
        }
        SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();
        PlayerProfile profile = getPlayerProfile(value);
        headMeta.setOwnerProfile(profile);
        itemStack.setItemMeta(headMeta);
        return itemStack;
    }

    @Override
    public ItemStack texture(ItemStack itemStack, String texture) {
        if (texture.isEmpty()) return itemStack;
        if (!texture.startsWith("https://textures.minecraft.net/texture/")) {
            texture = "https://textures.minecraft.net/texture/" + texture;
        }
        SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();
        PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures profileTextures = playerProfile.getTextures();
        try {
            profileTextures.setSkin(new URL(texture));
        } catch (MalformedURLException exception) {
            exception.printStackTrace();
        }
        playerProfile.setTextures(profileTextures);
        headMeta.setOwnerProfile(playerProfile);
        itemStack.setItemMeta(headMeta);
        return itemStack;
    }

    private PlayerProfile getPlayerProfile(String base64Url) {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

        String decodedBase64 = decodeSkinUrl(base64Url);
        if (decodedBase64 == null) {
            return profile;
        }

        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL(decodedBase64));
        } catch (final MalformedURLException exception) {
            exception.printStackTrace();
        }

        profile.setTextures(textures);
        return profile;
    }

    public String decodeSkinUrl(final String base64Texture) {
        String cleanBase64Texture = fixBase64Padding(base64Texture);
        try {
            final String decoded = new String(Base64.getDecoder().decode(cleanBase64Texture));

            JsonElement root = GSON.fromJson(decoded, JsonElement.class);

            if (!root.isJsonObject()) {
                System.out.println("decodeSkinUrl: decoded value is not a JSON object: " + decoded);
                return null;
            }

            JsonObject object = root.getAsJsonObject();

            JsonObject textures = object.getAsJsonObject("textures");
            if (textures == null) return null;

            JsonObject skin = textures.getAsJsonObject("SKIN");
            if (skin == null) return null;

            JsonElement url = skin.get("url");
            return url != null ? url.getAsString() : null;
        } catch (Exception e) {
            Bukkit.getLogger().info("Error: " + cleanBase64Texture + " track " + e.getMessage());
            return null;
        }
    }

    private String fixBase64Padding(String base64) {
        base64 = base64.trim().replace("\n", "").replace("\r", "");

        int mod = base64.length() % 4;
        if (mod != 0) {
            base64 += repeat("=", 4 - mod);
        }

        return base64;
    }

    private String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private String replaceColors(String message) {
        StringBuilder result = new StringBuilder(message.length());
        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (ch == '&' || ch == '§') {
                if (i + 1 < message.length()) {
                    char code = Character.toLowerCase(message.charAt(i + 1));
                    String replacement = replacements.get(String.valueOf(code));
                    if (replacement != null) {
                        result.append(replacement);
                        i++;
                    } else {
                        result.append(ch);
                    }
                } else {
                    result.append(ch);
                }
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

}