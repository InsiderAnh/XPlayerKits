package io.github.InsiderAnh.xPlayerKits.items;

import io.github.InsiderAnh.xPlayerKits.enums.MinecraftVersion;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionBannerPattern;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionEnchantment;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionPotionEffect;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSerializer {

    public static ItemStack deserialize(Map<String, Object> data) {
        ItemStack item = createBasicItemStack(data);
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        applyBasicMeta(meta, data);
        applyEnchantments(meta, data);
        applyItemFlags(meta, data);
        applyVersionSpecificMeta(meta, data);
        deserializeSpecificMeta(meta, data);

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createBasicItemStack(Map<String, Object> data) {
        String materialName = (String) data.get("material");
        if (materialName == null) return null;

        Material material = parseMaterial(materialName);
        if (material == null) return null;

        int amount = data.containsKey("amount") ? (Integer) data.get("amount") : 1;
        ItemStack item = new ItemStack(material, amount);

        if (data.containsKey("durability")) {
            item.setDurability(((Number) data.get("durability")).shortValue());
        }

        return item;
    }

    private static Material parseMaterial(String materialName) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void applyBasicMeta(ItemMeta meta, Map<String, Object> data) {
        if (data.containsKey("displayname")) {
            String displayName = (String) data.get("displayname");
            meta.setDisplayName(codeToColor(displayName));
        }

        if (data.containsKey("lore")) {
            List<String> lore = convertLoreList((List<?>) data.get("lore"));
            meta.setLore(lore);
        }
    }

    private static List<String> convertLoreList(List<?> loreList) {
        List<String> lore = new ArrayList<>();
        for (Object line : loreList) {
            lore.add(codeToColor(line.toString()));
        }
        return lore;
    }

    private static void applyEnchantments(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("enchants")) return;

        List<?> enchantList = (List<?>) data.get("enchants");
        for (Object enchantObj : enchantList) {
            applyEnchantment(meta, enchantObj.toString());
        }
    }

    private static void applyEnchantment(ItemMeta meta, String enchantStr) {
        String[] parts = enchantStr.split(":");
        if (parts.length < 2) return;

        Enchantment enchant = CrossVersionEnchantment.getEnchant(parts[0]);
        if (enchant != null) {
            int level = Integer.parseInt(parts[1]);
            meta.addEnchant(enchant, level, true);
        }
    }

    private static void applyItemFlags(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("itemflags")) return;

        List<?> flagList = (List<?>) data.get("itemflags");
        for (Object flagObj : flagList) {
            applyItemFlag(meta, flagObj.toString());
        }
    }

    private static void applyItemFlag(ItemMeta meta, String flagName) {
        try {
            ItemFlag itemFlag = ItemFlag.valueOf(flagName);
            meta.addItemFlags(itemFlag);
        } catch (Exception ignored) {
        }
    }

    private static void applyVersionSpecificMeta(ItemMeta meta, Map<String, Object> data) {
        applyUnbreakable(meta, data);
        applyCustomModelData(meta, data);
    }

    private static void applyUnbreakable(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("unbreakable")) return;
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_11)) return;

        try {
            Method setUnbreakable = meta.getClass().getMethod("setUnbreakable", boolean.class);
            setUnbreakable.invoke(meta, data.get("unbreakable"));
        } catch (Exception ignored) {
        }
    }

    private static void applyCustomModelData(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("custom_model_data")) return;
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_13)) return;

        try {
            Method setCustomModelData = meta.getClass().getMethod("setCustomModelData", Integer.class);
            setCustomModelData.invoke(meta, data.get("custom_model_data"));
        } catch (Exception ignored) {
        }
    }

    private static void deserializeSpecificMeta(ItemMeta meta, Map<String, Object> data) {
        if (meta instanceof BannerMeta) {
            deserializeBannerMeta((BannerMeta) meta, data);
        } else if (meta instanceof LeatherArmorMeta) {
            deserializeLeatherArmorMeta((LeatherArmorMeta) meta, data);
        } else if (meta instanceof SkullMeta) {
            deserializeSkullMeta((SkullMeta) meta, data);
        } else if (meta instanceof BookMeta) {
            deserializeBookMeta((BookMeta) meta, data);
        } else if (meta instanceof PotionMeta) {
            deserializePotionMeta((PotionMeta) meta, data);
        }
    }

    private static void deserializeBannerMeta(BannerMeta bannerMeta, Map<String, Object> data) {
        if (!data.containsKey("banner_patterns")) return;

        List<?> patternList = (List<?>) data.get("banner_patterns");
        List<Pattern> patterns = new ArrayList<>();

        for (Object patternObj : patternList) {
            Pattern pattern = parseBannerPattern(patternObj.toString());
            if (pattern != null) {
                patterns.add(pattern);
            }
        }
        bannerMeta.setPatterns(patterns);
    }

    private static Pattern parseBannerPattern(String patternStr) {
        String[] parts = patternStr.split(":");
        if (parts.length < 2) return null;

        PatternType type = CrossVersionBannerPattern.getPattern(parts[0]);
        if (type == null) return null;

        try {
            DyeColor color = DyeColor.valueOf(parts[1].toUpperCase());
            return new Pattern(color, type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void deserializeLeatherArmorMeta(LeatherArmorMeta leatherMeta, Map<String, Object> data) {
        if (data.containsKey("leather_color")) {
            int colorInt = (Integer) data.get("leather_color");
            leatherMeta.setColor(Color.fromRGB(colorInt));
        }
    }

    private static void deserializeSkullMeta(SkullMeta skullMeta, Map<String, Object> data) {
        if (data.containsKey("skull_owner")) {
            skullMeta.setOwner((String) data.get("skull_owner"));
        }
    }

    private static void deserializeBookMeta(BookMeta bookMeta, Map<String, Object> data) {
        if (data.containsKey("book_title")) {
            bookMeta.setTitle(codeToColor((String) data.get("book_title")));
        }
        if (data.containsKey("book_author")) {
            bookMeta.setAuthor((String) data.get("book_author"));
        }
        if (data.containsKey("book_pages")) {
            List<String> pages = convertBookPages((List<?>) data.get("book_pages"));
            bookMeta.setPages(pages);
        }
    }

    private static List<String> convertBookPages(List<?> pageList) {
        List<String> pages = new ArrayList<>();
        for (Object page : pageList) {
            pages.add(codeToColor(page.toString()));
        }
        return pages;
    }

    private static void deserializePotionMeta(PotionMeta potionMeta, Map<String, Object> data) {
        if (!data.containsKey("potion_effects")) return;

        List<?> effectList = (List<?>) data.get("potion_effects");
        for (Object effectObj : effectList) {
            PotionEffect effect = parsePotionEffect(effectObj.toString());
            if (effect != null) {
                potionMeta.addCustomEffect(effect, true);
            }
        }
    }

    private static PotionEffect parsePotionEffect(String effectStr) {
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

    public static void serialize(ItemStack item, YamlConfiguration config, String path) {
        if (item == null || item.getType() == Material.AIR) {
            config.set(path + ".material", "AIR");
            return;
        }

        config.set(path, null);
        serializeBasicProperties(item, config, path);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            serializeItemMeta(meta, config, path);
        }
    }

    private static void serializeBasicProperties(ItemStack item, YamlConfiguration config, String path) {
        config.set(path + ".material", item.getType().name());

        if (item.getAmount() != 1) {
            config.set(path + ".amount", item.getAmount());
        }

        if (item.getDurability() != 0) {
            config.set(path + ".durability", item.getDurability());
        }
    }

    private static void serializeItemMeta(ItemMeta meta, YamlConfiguration config, String path) {
        serializeDisplayProperties(meta, config, path);
        serializeEnchantments(meta, config, path);
        serializeItemFlags(meta, config, path);
        serializeVersionSpecificProperties(meta, config, path);
        serializeSpecificMeta(meta, config, path);
    }

    private static void serializeDisplayProperties(ItemMeta meta, YamlConfiguration config, String path) {
        if (meta.hasDisplayName()) {
            config.set(path + ".displayname", colorToCode(meta.getDisplayName()));
        }

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(colorToCode(line));
            }
            config.set(path + ".lore", lore);
        }
    }

    private static void serializeEnchantments(ItemMeta meta, YamlConfiguration config, String path) {
        if (!meta.hasEnchants()) return;

        List<String> enchants = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            String enchantId = CrossVersionEnchantment.getEnchantId(entry.getKey());
            enchants.add(enchantId + ":" + entry.getValue());
        }
        config.set(path + ".enchants", enchants);
    }

    private static void serializeItemFlags(ItemMeta meta, YamlConfiguration config, String path) {
        if (meta.getItemFlags().isEmpty()) return;

        List<String> flags = new ArrayList<>();
        for (Object flag : meta.getItemFlags()) {
            flags.add(flag.toString());
        }
        config.set(path + ".itemflags", flags);
    }

    private static void serializeVersionSpecificProperties(ItemMeta meta, YamlConfiguration config, String path) {
        serializeUnbreakable(meta, config, path);
        serializeCustomModelData(meta, config, path);
    }

    private static void serializeUnbreakable(ItemMeta meta, YamlConfiguration config, String path) {
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_11)) return;

        try {
            Method isUnbreakable = meta.getClass().getMethod("isUnbreakable");
            if ((Boolean) isUnbreakable.invoke(meta)) {
                config.set(path + ".unbreakable", true);
            }
        } catch (Exception ignored) {
        }
    }

    private static void serializeCustomModelData(ItemMeta meta, YamlConfiguration config, String path) {
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_13)) return;

        try {
            Method hasCustomModelData = meta.getClass().getMethod("hasCustomModelData");
            if ((Boolean) hasCustomModelData.invoke(meta)) {
                Method getCustomModelData = meta.getClass().getMethod("getCustomModelData");
                int modelData = (Integer) getCustomModelData.invoke(meta);
                config.set(path + ".custom_model_data", modelData);
            }
        } catch (Exception ignored) {
        }
    }

    private static void serializeSpecificMeta(ItemMeta meta, YamlConfiguration config, String path) {
        if (meta instanceof BannerMeta) {
            serializeBannerMeta((BannerMeta) meta, config, path);
        } else if (meta instanceof LeatherArmorMeta) {
            serializeLeatherArmorMeta((LeatherArmorMeta) meta, config, path);
        } else if (meta instanceof SkullMeta) {
            serializeSkullMeta((SkullMeta) meta, config, path);
        } else if (meta instanceof BookMeta) {
            serializeBookMeta((BookMeta) meta, config, path);
        } else if (meta instanceof PotionMeta) {
            serializePotionMeta((PotionMeta) meta, config, path);
        } else if (meta instanceof FireworkMeta) {
            serializeFireworkMeta((FireworkMeta) meta, config, path);
        } else if (meta instanceof MapMeta) {
            serializeMapMeta((MapMeta) meta, config, path);
        }
    }

    private static void serializeBannerMeta(BannerMeta bannerMeta, YamlConfiguration config, String path) {
        if (bannerMeta.getPatterns().isEmpty()) return;

        List<String> patterns = new ArrayList<>();
        for (Pattern pattern : bannerMeta.getPatterns()) {
            String patternId = CrossVersionBannerPattern.getPatternId(pattern.getPattern());
            String color = pattern.getColor().name().toLowerCase();
            patterns.add(patternId + ":" + color);
        }
        config.set(path + ".banner_patterns", patterns);
    }

    private static void serializeLeatherArmorMeta(LeatherArmorMeta leatherMeta, YamlConfiguration config, String path) {
        Color color = leatherMeta.getColor();
        if (!color.equals(Color.fromRGB(160, 101, 64))) {
            config.set(path + ".leather_color", color.asRGB());
        }
    }

    private static void serializeSkullMeta(SkullMeta skullMeta, YamlConfiguration config, String path) {
        if (skullMeta.hasOwner()) {
            config.set(path + ".skull_owner", skullMeta.getOwner());
        }
    }

    private static void serializeBookMeta(BookMeta bookMeta, YamlConfiguration config, String path) {
        if (bookMeta.hasTitle()) {
            config.set(path + ".book_title", colorToCode(bookMeta.getTitle()));
        }
        if (bookMeta.hasAuthor()) {
            config.set(path + ".book_author", bookMeta.getAuthor());
        }
        if (bookMeta.hasPages()) {
            List<String> pages = new ArrayList<>();
            for (String page : bookMeta.getPages()) {
                pages.add(colorToCode(page));
            }
            config.set(path + ".book_pages", pages);
        }
    }

    private static void serializePotionMeta(PotionMeta potionMeta, YamlConfiguration config, String path) {
        if (!potionMeta.hasCustomEffects()) return;

        List<String> effects = new ArrayList<>();
        for (PotionEffect effect : potionMeta.getCustomEffects()) {
            String effectId = CrossVersionPotionEffect.getEffectId(effect.getType());
            effects.add(effectId + ":" + (effect.getAmplifier() + 1) + ":" + effect.getDuration());
        }
        config.set(path + ".potion_effects", effects);
    }

    private static void serializeFireworkMeta(FireworkMeta fireworkMeta, YamlConfiguration config, String path) {
        config.set(path + ".firework_power", fireworkMeta.getPower());

        if (!fireworkMeta.getEffects().isEmpty()) {
            List<Map<String, Object>> effects = new ArrayList<>();
            for (FireworkEffect effect : fireworkMeta.getEffects()) {
                Map<String, Object> effectMap = createFireworkEffectMap(effect);
                effects.add(effectMap);
            }
            config.set(path + ".firework_effects", effects);
        }
    }

    private static Map<String, Object> createFireworkEffectMap(FireworkEffect effect) {
        Map<String, Object> effectMap = new HashMap<>();
        effectMap.put("type", effect.getType().name().toLowerCase());

        if (!effect.getColors().isEmpty()) {
            List<Integer> colors = new ArrayList<>();
            for (Color color : effect.getColors()) {
                colors.add(color.asRGB());
            }
            effectMap.put("colors", colors);
        }
        return effectMap;
    }

    private static void serializeMapMeta(MapMeta mapMeta, YamlConfiguration config, String path) {
        if (mapMeta.hasMapId()) {
            config.set(path + ".map_id", mapMeta.getMapId());
        }
    }

    private static String colorToCode(String text) {
        return text.replace("§", "&");
    }

    private static String codeToColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}