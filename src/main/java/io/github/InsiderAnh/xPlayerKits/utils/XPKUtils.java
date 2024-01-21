package io.github.InsiderAnh.xPlayerKits.utils;

import com.google.gson.Gson;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.enums.ServerVersion;
import io.github.InsiderAnh.xPlayerKits.utils.xseries.XSound;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class XPKUtils {

    public final ServerVersion SERVER_VERSION;
    @Getter
    private final Gson gson;
    @Getter
    private final JsonWriterSettings writerSettings;
    private final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    static {
        gson = new Gson();
        writerSettings = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
        SERVER_VERSION = ServerVersion.valueOf(Bukkit.getServer().getClass().getPackage().getName().replace("org.bukkit.craftbukkit.", ""));
    }

    public String getStatus(boolean bool) {
        return PlayerKits.getInstance().getLang().getString("messages." + (bool ? "enabled" : "disabled"));
    }

    public String color(String message) {
        return translateAlternateColorCodes('&', message);
    }

    public String translateAlternateColorCodes(char altColorChar, String message) {
        if (ServerVersion.v1_16_R1.serverVersionGreaterEqualThan(SERVER_VERSION)) {
            Matcher matcher = pattern.matcher(message);
            StringBuffer stringBuffer = new StringBuffer();
            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                try {
                    ChatColor chatColor = ChatColor.of(color);
                    matcher.appendReplacement(stringBuffer, chatColor.toString());
                } catch (Exception ignored) {
                }
            }
            matcher.appendTail(stringBuffer);
            message = stringBuffer.toString();
        }
        return ChatColor.translateAlternateColorCodes(altColorChar, message);
    }

    public ItemStack applySimpleTag(ItemStack item, String key, String value) {
        return PlayerKits.getInstance().getNbtEditor().setTag(item, key, value);
    }

    public boolean isHelmet(String material) {
        return material.endsWith("_HELMET") || material.equals("PLAYER_HEAD") || material.equals("SKULL_ITEM");
    }

    public boolean isChestplate(String material) {
        return material.endsWith("_CHESTPLATE") || material.equals("ELYTRA");
    }

    public boolean isLeggings(String material) {
        return material.endsWith("_LEGGINGS");
    }

    public boolean isBoots(String material) {
        return material.endsWith("_BOOTS");
    }

    public void executeActions(Player player, ArrayList<String> actions) {
        for (String action : actions) {
            String actionType = action.split(":")[0].toLowerCase();
            String actionData = action.split(":")[1];
            switch (actionType) {
                case "console":
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), actionData.replaceFirst("/", ""));
                    break;
                case "command":
                    player.chat(actionData);
                    break;
                case "sound":
                    String[] subData = actionData.split(";");
                    try {
                        Sound sound = XSound.matchXSound(subData[0]).orElse(XSound.BLOCK_NOTE_BLOCK_PLING).parseSound();
                        float volume = Float.parseFloat(subData[1]);
                        float pitch = Float.parseFloat(subData[2]);
                        if (sound != null) {
                            player.playSound(player.getLocation(), sound, volume, pitch);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        PlayerKits.getInstance().getLogger().warning("Error on execute sound format.");
                    }
                    break;
            }
        }
    }

    public boolean passCondition(Player player, String condition) {
        String[] sep = condition.split(" ");
        String variable = PlaceholderAPI.setPlaceholders(player, sep[0]);
        String conditional = sep[1];

        try {
            double valueFinal = Double.parseDouble(sep[2]);
            double valueFinalVariable = Double.parseDouble(variable);

            switch (conditional) {
                case ">=":
                    return valueFinalVariable >= valueFinal;
                case "<=":
                    return valueFinalVariable <= valueFinal;
                case "==":
                    return variable.equals(sep[2]);
                case "!=":
                    return !variable.equals(sep[2]);
                case ">":
                    return valueFinalVariable > valueFinal;
                case "<":
                    return valueFinalVariable < valueFinal;
                default:
                    return false;
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public String millisToLongDHMS(long millis) {
        StringBuilder time = new StringBuilder();
        long l;
        boolean space = false;
        if (millis >= 1000L) {
            l = millis / 86400000L;
            if (l > 0L) {
                space = true;
                millis -= l * 86400000L;
                time.append(l).append("d");
            }
            if (space) {
                time.append(" ");
                space = false;
            }
            l = millis / 3600000L;
            if (l > 0L) {
                space = true;
                millis -= l * 3600000L;
                time.append(l).append("h");
            }
            if (space) {
                time.append(" ");
                space = false;
            }
            l = millis / 60000L;
            if (l > 0L) {
                space = true;
                millis -= l * 60000L;
                time.append(l).append("m");
            }
            l = millis / 1000L;
            if (l > 0L) {
                if (space) {
                    time.append(" ");
                }
                time.append(l).append((l > 1L) ? "s" : "");
            }
            return time.toString();
        }
        return "0";
    }

}