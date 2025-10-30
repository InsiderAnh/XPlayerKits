package io.github.InsiderAnh.xPlayerKits.items;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.enums.MinecraftVersion;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionBannerPattern;
import io.github.InsiderAnh.xPlayerKits.items.versions.CrossVersionEnchantment;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.Method;
import java.util.*;

public class ItemSerializer {

    public static ItemStack deserialize(Map<String, Object> data) {
        ItemStack item = createBasicItemStack(data);
        if (item == null) return null;

        applyNBT(item, data);

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
        short dat = data.containsKey("data") ? (short) (int) data.get("data") : 0;
        ItemStack item = new ItemStack(material, amount, dat);

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

    private static void applyNBT(ItemStack item, Map<String, Object> data) {
        if (!data.containsKey("nbt_data")) return;

        String nbtString = (String) data.get("nbt_data");
        if (nbtString == null) return;

        ReadableNBT nbt = NBT.parseNBT(nbtString);
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.mergeCompound(nbt);
        item.setItemMeta(nbtItem.getItem().getItemMeta());
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
        applyAttributeModifiers(meta, data);
        applyArmorTrim(meta, data);
    }

    private static void applyUnbreakable(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("unbreakable")) return;

        boolean unbreakable = (boolean) data.get("unbreakable");
        PlayerKits.getInstance().getPlayerKitsNMS().setUnbreakable(meta, unbreakable);
    }

    private static void applyCustomModelData(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("custom_model_data")) return;
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_13)) return;

        int customModelData = (Integer) data.get("custom_model_data");
        if (customModelData == 0) return;

        PlayerKits.getInstance().getPlayerKitsNMS().setCustomModelData(meta, customModelData);
    }

    private static void applyAttributeModifiers(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("attribute_modifiers")) return;
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_13)) return;

        try {
            List<?> attributeList = (List<?>) data.get("attribute_modifiers");
            for (Object attrObj : attributeList) {
                if (!(attrObj instanceof Map)) continue;
                Map<String, Object> attrMap = (Map<String, Object>) attrObj;

                String attributeName = (String) attrMap.get("attribute");
                Attribute attribute = Attribute.valueOf(attributeName);

                String name = (String) attrMap.get("name");
                double amount = ((Number) attrMap.get("amount")).doubleValue();
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf((String) attrMap.get("operation"));

                EquipmentSlot slot = null;
                if (attrMap.containsKey("slot")) {
                    slot = EquipmentSlot.valueOf((String) attrMap.get("slot"));
                }

                UUID uuid = attrMap.containsKey("uuid") ? UUID.fromString((String) attrMap.get("uuid")) : UUID.randomUUID();

                AttributeModifier modifier;
                if (slot != null) {
                    modifier = new AttributeModifier(uuid, name, amount, operation, slot);
                } else {
                    modifier = new AttributeModifier(uuid, name, amount, operation);
                }

                meta.addAttributeModifier(attribute, modifier);
            }
        } catch (Exception ignored) {
        }
    }

    private static void applyArmorTrim(ItemMeta meta, Map<String, Object> data) {
        if (!data.containsKey("armor_trim")) return;
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_20)) return;
        if (!(meta instanceof ArmorMeta)) return;

        try {
            Map<String, String> trimData = (Map<String, String>) data.get("armor_trim");
            String materialKey = trimData.get("material");
            String patternKey = trimData.get("pattern");

            Class<?> trimMaterialClass = Class.forName("org.bukkit.inventory.meta.trim.TrimMaterial");
            Class<?> trimPatternClass = Class.forName("org.bukkit.inventory.meta.trim.TrimPattern");
            Class<?> armorTrimClass = Class.forName("org.bukkit.inventory.meta.trim.ArmorTrim");

            Object registry = Class.forName("org.bukkit.Registry").getField("TRIM_MATERIAL").get(null);
            Method getMethod = registry.getClass().getMethod("get", Class.forName("org.bukkit.NamespacedKey"));
            Object trimMaterial = getMethod.invoke(registry, Class.forName("org.bukkit.NamespacedKey").getConstructor(String.class, String.class).newInstance("minecraft", materialKey));

            registry = Class.forName("org.bukkit.Registry").getField("TRIM_PATTERN").get(null);
            Object trimPattern = getMethod.invoke(registry, Class.forName("org.bukkit.NamespacedKey").getConstructor(String.class, String.class).newInstance("minecraft", patternKey));

            if (trimMaterial != null && trimPattern != null) {
                Object armorTrim = armorTrimClass.getConstructor(trimMaterialClass, trimPatternClass).newInstance(trimMaterial, trimPattern);
                ArmorMeta armorMeta = (ArmorMeta) meta;
                armorMeta.getClass().getMethod("setTrim", armorTrimClass).invoke(armorMeta, armorTrim);
            }
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
        } else if (meta instanceof FireworkMeta) {
            deserializeFireworkMeta((FireworkMeta) meta, data);
        } else if (meta instanceof MapMeta) {
            deserializeMapMeta((MapMeta) meta, data);
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

    private static void deserializeFireworkMeta(FireworkMeta fireworkMeta, Map<String, Object> data) {
        if (data.containsKey("firework_power")) {
            fireworkMeta.setPower((Integer) data.get("firework_power"));
        }

        if (data.containsKey("firework_effects")) {
            List<?> effectsList = (List<?>) data.get("firework_effects");
            for (Object effectObj : effectsList) {
                if (!(effectObj instanceof Map)) continue;
                Map<String, Object> effectMap = (Map<String, Object>) effectObj;

                FireworkEffect effect = parseFireworkEffect(effectMap);
                if (effect != null) {
                    fireworkMeta.addEffect(effect);
                }
            }
        }
    }

    private static FireworkEffect parseFireworkEffect(Map<String, Object> effectMap) {
        try {
            String typeStr = (String) effectMap.get("type");
            FireworkEffect.Type type = FireworkEffect.Type.valueOf(typeStr.toUpperCase());

            FireworkEffect.Builder builder = FireworkEffect.builder().with(type);

            if (effectMap.containsKey("colors")) {
                List<?> colorsList = (List<?>) effectMap.get("colors");
                for (Object colorObj : colorsList) {
                    int colorInt = (Integer) colorObj;
                    builder.withColor(Color.fromRGB(colorInt));
                }
            }

            if (effectMap.containsKey("fade_colors")) {
                List<?> fadeColorsList = (List<?>) effectMap.get("fade_colors");
                for (Object colorObj : fadeColorsList) {
                    int colorInt = (Integer) colorObj;
                    builder.withFade(Color.fromRGB(colorInt));
                }
            }

            if (effectMap.containsKey("flicker")) {
                builder.flicker((Boolean) effectMap.get("flicker"));
            }

            if (effectMap.containsKey("trail")) {
                builder.trail((Boolean) effectMap.get("trail"));
            }

            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static void deserializeMapMeta(MapMeta mapMeta, Map<String, Object> data) {
        if (data.containsKey("map_id")) {
            try {
                Method setMapId = mapMeta.getClass().getMethod("setMapId", int.class);
                setMapId.invoke(mapMeta, (Integer) data.get("map_id"));
            } catch (Exception ignored) {
            }
        }
    }

    public static void serialize(ItemStack item, YamlConfiguration config, String path) {
        if (item == null || item.getType() == Material.AIR) {
            config.set(path + ".material", "AIR");
            return;
        }

        config.set(path, null);
        serializeBasicProperties(item, config, path);
        serializeNBT(item, config, path);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            serializeItemMeta(item, meta, config, path);
        }
    }

    private static void serializeBasicProperties(ItemStack item, YamlConfiguration config, String path) {
        config.set(path + ".material", item.getType().name());

        if (item.getData().getData() != 0) {
            config.set(path + ".data", item.getData().getData());
        }

        if (item.getAmount() != 1) {
            config.set(path + ".amount", item.getAmount());
        }

        if (item.getDurability() != 0) {
            config.set(path + ".durability", item.getDurability());
        }
    }

    private static void serializeItemMeta(ItemStack itemStack, ItemMeta meta, YamlConfiguration config, String path) {
        serializeDisplayProperties(meta, config, path);
        serializeEnchantments(meta, config, path);
        serializeItemFlags(meta, config, path);
        serializeVersionSpecificProperties(meta, config, path);
        serializeSpecificMeta(itemStack, meta, config, path);
    }

    private static void serializeDisplayProperties(ItemMeta meta, YamlConfiguration config, String path) {
        if (meta.hasDisplayName()) {
            config.set(path + ".displayname", meta.getDisplayName());
        }

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : meta.getLore()) {
                lore.add(line);
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

    private static void serializeNBT(ItemStack itemStack, YamlConfiguration config, String path) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasNBTData()) return;

        config.set(path + ".nbt_data", nbtItem.toString());
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
        serializeAttributeModifiers(meta, config, path);
        serializeArmorTrim(meta, config, path);
    }

    private static void serializeUnbreakable(ItemMeta meta, YamlConfiguration config, String path) {
        try {
            config.set(path + ".unbreakable", PlayerKits.getInstance().getPlayerKitsNMS().isUnbreakable(meta));
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

    private static void serializeAttributeModifiers(ItemMeta meta, YamlConfiguration config, String path) {
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_13)) return;

        try {
            if (!meta.hasAttributeModifiers()) return;

            List<Map<String, Object>> attributeList = new ArrayList<>();
            for (Attribute attribute : meta.getAttributeModifiers().keySet()) {
                for (AttributeModifier modifier : meta.getAttributeModifiers(attribute)) {
                    Map<String, Object> attrMap = new HashMap<>();
                    attrMap.put("attribute", attribute.name());
                    attrMap.put("name", modifier.getName());
                    attrMap.put("amount", modifier.getAmount());
                    attrMap.put("operation", modifier.getOperation().name());
                    attrMap.put("uuid", modifier.getUniqueId().toString());

                    if (modifier.getSlot() != null) {
                        attrMap.put("slot", modifier.getSlot().name());
                    }

                    attributeList.add(attrMap);
                }
            }

            if (!attributeList.isEmpty()) {
                config.set(path + ".attribute_modifiers", attributeList);
            }
        } catch (Exception ignored) {
        }
    }

    private static void serializeArmorTrim(ItemMeta meta, YamlConfiguration config, String path) {
        if (!XPKUtils.SERVER_VERSION.greaterThanOrEqualTo(MinecraftVersion.v1_20)) return;
        if (!(meta instanceof ArmorMeta)) return;

        try {
            ArmorMeta armorMeta = (ArmorMeta) meta;
            Method hasTrim = armorMeta.getClass().getMethod("hasTrim");
            if ((Boolean) hasTrim.invoke(armorMeta)) {
                Method getTrim = armorMeta.getClass().getMethod("getTrim");
                Object armorTrim = getTrim.invoke(armorMeta);

                if (armorTrim != null) {
                    Class<?> armorTrimClass = Class.forName("org.bukkit.inventory.meta.trim.ArmorTrim");
                    Method getMaterial = armorTrimClass.getMethod("getMaterial");
                    Method getPattern = armorTrimClass.getMethod("getPattern");

                    Object trimMaterial = getMaterial.invoke(armorTrim);
                    Object trimPattern = getPattern.invoke(armorTrim);

                    Class<?> keyedClass = Class.forName("org.bukkit.Keyed");
                    Method getKey = keyedClass.getMethod("getKey");

                    Object materialKey = getKey.invoke(trimMaterial);
                    Object patternKey = getKey.invoke(trimPattern);

                    Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
                    Method getKeyMethod = namespacedKeyClass.getMethod("getKey");

                    String materialKeyStr = (String) getKeyMethod.invoke(materialKey);
                    String patternKeyStr = (String) getKeyMethod.invoke(patternKey);

                    Map<String, String> trimData = new HashMap<>();
                    trimData.put("material", materialKeyStr);
                    trimData.put("pattern", patternKeyStr);

                    config.set(path + ".armor_trim", trimData);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void serializeSpecificMeta(ItemStack itemStack, ItemMeta meta, YamlConfiguration config, String path) {
        if (meta instanceof BannerMeta) {
            serializeBannerMeta((BannerMeta) meta, config, path);
        } else if (meta instanceof LeatherArmorMeta) {
            serializeLeatherArmorMeta((LeatherArmorMeta) meta, config, path);
        } else if (meta instanceof SkullMeta) {
            serializeSkullMeta((SkullMeta) meta, config, path);
        } else if (meta instanceof BookMeta) {
            serializeBookMeta((BookMeta) meta, config, path);
        } else if (meta instanceof PotionMeta) {
            serializePotionMeta(itemStack, config, path);
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
            config.set(path + ".book_title", bookMeta.getTitle());
        }
        if (bookMeta.hasAuthor()) {
            config.set(path + ".book_author", bookMeta.getAuthor());
        }
        if (bookMeta.hasPages()) {
            List<String> pages = new ArrayList<>();
            for (String page : bookMeta.getPages()) {
                pages.add(page);
            }
            config.set(path + ".book_pages", pages);
        }
    }

    private static void serializePotionMeta(ItemStack itemStack, YamlConfiguration config, String path) {
        PlayerKits.getInstance().getPlayerKitsNMS().serializePotionMeta(itemStack, config, path);
    }

    private static void deserializePotionMeta(PotionMeta potionMeta, Map<String, Object> data) {
        PlayerKits.getInstance().getPlayerKitsNMS().deserializePotionMeta(potionMeta, data);
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

        if (!effect.getFadeColors().isEmpty()) {
            List<Integer> fadeColors = new ArrayList<>();
            for (Color color : effect.getFadeColors()) {
                fadeColors.add(color.asRGB());
            }
            effectMap.put("fade_colors", fadeColors);
        }

        effectMap.put("flicker", effect.hasFlicker());
        effectMap.put("trail", effect.hasTrail());

        return effectMap;
    }

    private static void serializeMapMeta(MapMeta mapMeta, YamlConfiguration config, String path) {
        if (mapMeta.hasMapId()) {
            config.set(path + ".map_id", mapMeta.getMapId());
        }
    }

    private static String codeToColor(String text) {
        return PlayerKits.getInstance().getColorUtils().color(text);
    }

}