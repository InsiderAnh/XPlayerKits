package io.github.InsiderAnh.xPlayerKits.nms.v1_21_R6;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.InsiderAnh.xPlayerKits.api.PlayerKitsNMS;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class PlayerKitsNMS_v1_21_R6 extends PlayerKitsNMS {

    private final Gson GSON = new Gson();

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

        if (glowing) {
            itemMeta.setEnchantmentGlintOverride(true);
        } else {
            if (itemMeta.hasEnchantmentGlintOverride()) {
                itemMeta.setEnchantmentGlintOverride(false);
            }
        }
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

}