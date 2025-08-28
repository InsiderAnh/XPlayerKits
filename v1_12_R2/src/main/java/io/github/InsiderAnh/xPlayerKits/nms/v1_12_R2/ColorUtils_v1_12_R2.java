package io.github.InsiderAnh.xPlayerKits.nms.v1_12_R2;

import io.github.InsiderAnh.xPlayerKits.api.ColorUtils;
import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColorUtils_v1_12_R2 extends ColorUtils {

    private final Pattern pattern = Pattern.compile("&?#[a-fA-F0-9]{6}");

    public List<String> color(List<String> message) {
        return message.stream().map(this::color).collect(Collectors.toList());
    }

    public String color(String message) {
        return translateAlternateColorCodes('&', message);
    }

    public String translateAlternateColorCodes(char altColorChar, String message) {
        return ChatColor.translateAlternateColorCodes(altColorChar, message);
    }

}