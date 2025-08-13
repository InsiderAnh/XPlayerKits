package io.github.InsiderAnh.xPlayerKits.api;

import java.util.List;

public abstract class ColorUtils {

    public abstract List<String> color(List<String> message);

    public abstract String color(String message);

    public abstract String translateAlternateColorCodes(char altColorChar, String message);

}