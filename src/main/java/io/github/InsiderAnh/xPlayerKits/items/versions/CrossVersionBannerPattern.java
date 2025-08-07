package io.github.InsiderAnh.xPlayerKits.items.versions;

import org.bukkit.block.banner.PatternType;

import java.util.HashMap;
import java.util.Map;

public class CrossVersionBannerPattern {

    private static final Map<String, PatternType> PATTERN_MAP = new HashMap<>();
    private static boolean initialized = false;

    static {
        initializePatterns();
    }

    private static void initializePatterns() {
        if (initialized) return;

        for (PatternType type : PatternType.values()) {
            PATTERN_MAP.put(type.name().toLowerCase(), type);
            String name = type.name().toLowerCase();
            PATTERN_MAP.put(name.replace("_", ""), type);
        }

        initialized = true;
    }

    public static PatternType getPattern(String id) {
        return PATTERN_MAP.get(id.toLowerCase().replace("_", ""));
    }

    public static String getPatternId(PatternType pattern) {
        return pattern.name().toLowerCase();
    }

}