package io.github.InsiderAnh.xPlayerKits.items.versions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class CrossVersionPotionEffect {

    private static final Map<String, PotionEffectType> EFFECT_MAP = new HashMap<>();
    private static boolean initialized = false;

    static {
        initializeEffects();
    }

    private static void initializeEffects() {
        if (initialized) return;

        Map<String, String[]> effectAliases = new HashMap<>();

        effectAliases.put("speed", new String[]{"SPEED", "1"});
        effectAliases.put("slowness", new String[]{"SLOW", "SLOWNESS", "2"});
        effectAliases.put("haste", new String[]{"FAST_DIGGING", "HASTE", "3"});
        effectAliases.put("mining_fatigue", new String[]{"SLOW_DIGGING", "MINING_FATIGUE", "4"});
        effectAliases.put("strength", new String[]{"INCREASE_DAMAGE", "STRENGTH", "5"});
        effectAliases.put("instant_health", new String[]{"HEAL", "INSTANT_HEALTH", "6"});
        effectAliases.put("instant_damage", new String[]{"HARM", "INSTANT_DAMAGE", "7"});
        effectAliases.put("jump_boost", new String[]{"JUMP", "JUMP_BOOST", "8"});
        effectAliases.put("nausea", new String[]{"CONFUSION", "NAUSEA", "9"});
        effectAliases.put("regeneration", new String[]{"REGENERATION", "10"});
        effectAliases.put("resistance", new String[]{"DAMAGE_RESISTANCE", "RESISTANCE", "11"});
        effectAliases.put("fire_resistance", new String[]{"FIRE_RESISTANCE", "12"});
        effectAliases.put("water_breathing", new String[]{"WATER_BREATHING", "13"});
        effectAliases.put("invisibility", new String[]{"INVISIBILITY", "14"});
        effectAliases.put("blindness", new String[]{"BLINDNESS", "15"});
        effectAliases.put("night_vision", new String[]{"NIGHT_VISION", "16"});
        effectAliases.put("hunger", new String[]{"HUNGER", "17"});
        effectAliases.put("weakness", new String[]{"WEAKNESS", "18"});
        effectAliases.put("poison", new String[]{"POISON", "19"});
        effectAliases.put("wither", new String[]{"WITHER", "20"});
        effectAliases.put("health_boost", new String[]{"HEALTH_BOOST", "21"});
        effectAliases.put("absorption", new String[]{"ABSORPTION", "22"});
        effectAliases.put("saturation", new String[]{"SATURATION", "23"});
        effectAliases.put("glowing", new String[]{"GLOWING", "24"});
        effectAliases.put("levitation", new String[]{"LEVITATION", "25"});
        effectAliases.put("luck", new String[]{"LUCK", "26"});
        effectAliases.put("unluck", new String[]{"UNLUCK", "27"});
        effectAliases.put("slow_falling", new String[]{"SLOW_FALLING", "28"});
        effectAliases.put("conduit_power", new String[]{"CONDUIT_POWER", "29"});
        effectAliases.put("dolphins_grace", new String[]{"DOLPHINS_GRACE", "30"});
        effectAliases.put("bad_omen", new String[]{"BAD_OMEN", "31"});
        effectAliases.put("hero_of_the_village", new String[]{"HERO_OF_THE_VILLAGE", "32"});

        for (Map.Entry<String, String[]> entry : effectAliases.entrySet()) {
            String key = entry.getKey();
            String[] aliases = entry.getValue();

            for (String alias : aliases) {
                try {
                    PotionEffectType effect = PotionEffectType.getByName(alias);
                    if (effect != null) {
                        EFFECT_MAP.put(key, effect);
                        break;
                    }
                    if (alias.matches("\\d+")) {
                        effect = PotionEffectType.getById(Integer.parseInt(alias));
                        if (effect != null) {
                            EFFECT_MAP.put(key, effect);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    PlayerKits.getInstance().getLogger().info("Error on initialize potion effect: " + ex.getMessage());
                }
            }
        }

        initialized = true;
    }

    public static PotionEffectType getEffect(String id) {
        return EFFECT_MAP.get(id.toLowerCase());
    }

    public static String getEffectId(PotionEffectType effect) {
        for (Map.Entry<String, PotionEffectType> entry : EFFECT_MAP.entrySet()) {
            if (entry.getValue().equals(effect)) {
                return entry.getKey();
            }
        }
        return effect.getName().toLowerCase();
    }

}