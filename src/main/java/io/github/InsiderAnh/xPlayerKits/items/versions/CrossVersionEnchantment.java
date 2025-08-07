package io.github.InsiderAnh.xPlayerKits.items.versions;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CrossVersionEnchantment {

    private static final Map<String, Enchantment> ENCHANTMENT_MAP = new HashMap<>();
    private static boolean initialized = false;

    static {
        initializeEnchantments();
    }

    private static void initializeEnchantments() {
        if (initialized) return;

        Map<String, String[]> enchantAliases = new HashMap<>();

        enchantAliases.put("protection", new String[]{"PROTECTION_ENVIRONMENTAL", "PROTECTION"});
        enchantAliases.put("fire_protection", new String[]{"PROTECTION_FIRE", "FIRE_PROTECTION"});
        enchantAliases.put("feather_falling", new String[]{"PROTECTION_FALL", "FEATHER_FALLING"});
        enchantAliases.put("blast_protection", new String[]{"PROTECTION_EXPLOSIONS", "BLAST_PROTECTION"});
        enchantAliases.put("projectile_protection", new String[]{"PROTECTION_PROJECTILE", "PROJECTILE_PROTECTION"});
        enchantAliases.put("respiration", new String[]{"OXYGEN", "RESPIRATION"});
        enchantAliases.put("aqua_affinity", new String[]{"WATER_WORKER", "AQUA_AFFINITY"});
        enchantAliases.put("thorns", new String[]{"THORNS"});
        enchantAliases.put("depth_strider", new String[]{"DEPTH_STRIDER"});
        enchantAliases.put("frost_walker", new String[]{"FROST_WALKER"});

        enchantAliases.put("sharpness", new String[]{"DAMAGE_ALL", "SHARPNESS"});
        enchantAliases.put("smite", new String[]{"DAMAGE_UNDEAD", "SMITE"});
        enchantAliases.put("bane_of_arthropods", new String[]{"DAMAGE_ARTHROPODS", "BANE_OF_ARTHROPODS"});
        enchantAliases.put("knockback", new String[]{"KNOCKBACK"});
        enchantAliases.put("fire_aspect", new String[]{"FIRE_ASPECT"});
        enchantAliases.put("looting", new String[]{"LOOT_BONUS_MOBS", "LOOTING"});
        enchantAliases.put("sweeping", new String[]{"SWEEPING_EDGE"});

        enchantAliases.put("efficiency", new String[]{"DIG_SPEED", "EFFICIENCY"});
        enchantAliases.put("silk_touch", new String[]{"SILK_TOUCH"});
        enchantAliases.put("unbreaking", new String[]{"DURABILITY", "UNBREAKING"});
        enchantAliases.put("fortune", new String[]{"LOOT_BONUS_BLOCKS", "FORTUNE"});

        enchantAliases.put("power", new String[]{"ARROW_DAMAGE", "POWER"});
        enchantAliases.put("punch", new String[]{"ARROW_KNOCKBACK", "PUNCH"});
        enchantAliases.put("flame", new String[]{"ARROW_FIRE", "FLAME"});
        enchantAliases.put("infinity", new String[]{"ARROW_INFINITE", "INFINITY"});

        enchantAliases.put("luck_of_the_sea", new String[]{"LUCK", "LUCK_OF_THE_SEA"});
        enchantAliases.put("lure", new String[]{"LURE"});

        enchantAliases.put("mending", new String[]{"MENDING"});
        enchantAliases.put("curse_of_binding", new String[]{"BINDING_CURSE"});
        enchantAliases.put("curse_of_vanishing", new String[]{"VANISHING_CURSE"});
        enchantAliases.put("impaling", new String[]{"IMPALING"});
        enchantAliases.put("riptide", new String[]{"RIPTIDE"});
        enchantAliases.put("loyalty", new String[]{"LOYALTY"});
        enchantAliases.put("channeling", new String[]{"CHANNELING"});
        enchantAliases.put("multishot", new String[]{"MULTISHOT"});
        enchantAliases.put("quick_charge", new String[]{"QUICK_CHARGE"});
        enchantAliases.put("piercing", new String[]{"PIERCING"});

        for (Map.Entry<String, String[]> entry : enchantAliases.entrySet()) {
            String key = entry.getKey();
            String[] aliases = entry.getValue();

            for (String alias : aliases) {
                try {
                    Enchantment ench = Enchantment.getByName(alias);
                    if (ench != null) {
                        ENCHANTMENT_MAP.put(key, ench);
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        initialized = true;
    }

    public static Enchantment getEnchant(String id) {
        return ENCHANTMENT_MAP.get(id.toLowerCase());
    }

    public static String getEnchantId(Enchantment enchantment) {
        for (Map.Entry<String, Enchantment> entry : ENCHANTMENT_MAP.entrySet()) {
            if (entry.getValue().equals(enchantment)) {
                return entry.getKey();
            }
        }
        return enchantment.getName().toLowerCase();
    }

    public static Set<String> getAllEnchantIds() {
        return ENCHANTMENT_MAP.keySet();
    }

}