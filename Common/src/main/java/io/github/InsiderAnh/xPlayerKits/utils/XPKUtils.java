package io.github.InsiderAnh.xPlayerKits.utils;

import com.google.gson.Gson;
import de.tr7zw.changeme.nbtapi.NBTItem;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.api.events.ClaimXKitEvent;
import io.github.InsiderAnh.xPlayerKits.commands.XKitsCommands;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.enums.MinecraftVersion;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyTiming;
import io.github.InsiderAnh.xPlayerKits.libs.xseries.XSound;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class XPKUtils {

    public final MinecraftVersion SERVER_VERSION;
    public final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
    private final PlayerKits playerKits = PlayerKits.getInstance();
    @Getter
    private final Gson gson;
    @Getter
    private final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    static {
        gson = new Gson();

        String cbPackage = Bukkit.getServer().getClass().getPackage().getName();
        String detectedVersion = cbPackage.substring(cbPackage.lastIndexOf('.') + 1);
        if (!detectedVersion.startsWith("v")) {
            detectedVersion = Bukkit.getServer().getBukkitVersion();
        }

        SERVER_VERSION = MinecraftVersion.get(detectedVersion);
    }

    public void claimKit(Player player, Kit kit, PlayerKitData playerKitData) {
        if (kit.isNoHasRequirements(player)) {
            player.sendMessage(playerKits.getLang().getString("messages.noRequirements"));
            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
            return;
        }

        if (!kit.getPermission().equals("none") && !player.hasPermission(kit.getPermission())) {
            XPKUtils.executeActions(player, kit.getActionsOnDeny());
            player.sendMessage(playerKits.getLang().getString("messages.noPermissionKit"));
            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
            return;
        }

        KitData kitData = playerKitData.getKitsData().get(kit.getName());
        PropertyTiming timing = kit.getPropertyTiming();
        if (timing.isOneTime()) {
            if (kitData != null && kitData.isOneTime() && !player.hasPermission("xkits.onetime.bypass")) {
                XPKUtils.executeActions(player, kit.getActionsOnDeny());
                player.sendMessage(playerKits.getLang().getString("messages.alreadyOneTime"));
                player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
                return;
            }
        }

        if (kitData != null && kitData.getCountdown() > System.currentTimeMillis() && !player.hasPermission("xkits.countdown.bypass")) {
            XPKUtils.executeActions(player, kit.getActionsOnDeny());
            player.sendMessage(playerKits.getLang().getString("messages.waitCountdown").replace("<time>", XPKUtils.millisToLongDHMS(kitData.getCountdown() - System.currentTimeMillis())));
            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
            return;
        }

        if (kit.getPropertyInventory().isCheckInventorySpace() && kit.isNoInventorySpace(player)) {
            XPKUtils.executeActions(player, kit.getActionsOnDeny());
            player.sendMessage(playerKits.getLang().getString("messages.noInventorySpace"));
            player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.get(), 1.0f, 1.0f);
            return;
        }

        ClaimXKitEvent event = new ClaimXKitEvent(player, kit);
        playerKits.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        playerKitData.getKitsData().put(kit.getName(), new KitData(kit.getName(), System.currentTimeMillis() + (timing.getCountdown() * 1000L), timing.isOneTime(), false));
        playerKits.getExecutor().execute(() -> {
            playerKits.getDatabase().updatePlayerData(player.getUniqueId());
            playerKits.getStellarTaskHook(() -> kit.giveKit(player)).runTask(player.getLocation());
        });
    }

    public String getStatus(boolean bool) {
        return PlayerKits.getInstance().getLang().getString("messages." + (bool ? "enabled" : "disabled"));
    }

    @Deprecated
    public String color(String message) {
        return PlayerKits.getInstance().getColorUtils().translateAlternateColorCodes('&', message);
    }

    @Deprecated
    public String translateAlternateColorCodes(char altColorChar, String message) {
        if (SERVER_VERSION.lessThanOrEqualTo(MinecraftVersion.v1_16)) {
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
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(key, value);
        nbtItem.mergeNBT(item);
        return item;
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

    public void executeActions(Player player, List<Execution> listToExecute) {
        PlayerKits.getInstance().getExecutionManager().execute(player, listToExecute, new Placeholder("<player>", player.getName()));
    }

    public boolean passCondition(Player player, String condition) {
        String[] sep = condition.split(" ");
        String variable = PlaceholderAPI.setPlaceholders(player, sep[0]);
        String conditional = sep[1];

        try {
            switch (conditional) {
                case "==":
                    return variable.equals(sep[2]);
                case "!=":
                    return !variable.equals(sep[2]);
                case ">=":
                case "<=":
                case ">":
                case "<":
                    double valueFinal = Double.parseDouble(sep[2]);
                    double valueFinalVariable = Double.parseDouble(variable);
                    switch (conditional) {
                        case ">=":
                            return valueFinalVariable >= valueFinal;
                        case "<=":
                            return valueFinalVariable <= valueFinal;
                        case ">":
                            return valueFinalVariable > valueFinal;
                        case "<":
                            return valueFinalVariable < valueFinal;
                        default:
                            return false;
                    }
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

    public ItemStack parseIcon(String path, YamlConfiguration configuration) {
        ItemStack itemStack = parseItemStack(path, configuration);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemStack.setAmount(configuration.getInt(path + ".amount"));

        if (configuration.getInt(path + ".custom-model-data") != 0) {
            playerKits.getPlayerKitsNMS().setCustomModelData(itemMeta, configuration.getInt(path + ".custom-model-data"));
        }
        if (configuration.getBoolean(path + ".glowing")) {
            playerKits.getPlayerKitsNMS().setGlowing(itemMeta, true);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack parseItemStack(String path, YamlConfiguration configuration) {
        String type = configuration.getString(path + ".material");
        if (type.startsWith("head:") || type.startsWith("player_head:")) {
            return new ItemStack(Material.STONE);
        } else {
            return new ItemStack(Material.valueOf(type));
        }
    }

    public ItemStack parseItemStack(Player player, String material) {
        if (material.startsWith("head:") || material.startsWith("player_head:")) {
            if (player == null) return new ItemStack(Material.STONE);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);

            String name = material.replaceFirst("player_head:", "").replace("%player_name%", player.getName());
            if (name.startsWith("http://textures.minecraft.net/texture/")) {
                playerKits.getPlayerKitsNMS().texture(head, name);
                return head;
            }
            if (name.length() > 16) {
                playerKits.getPlayerKitsNMS().value(head, name);
                return head;
            }

            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setOwner(material.replaceFirst("player_head:", "").replace("%player_name%", player.getName()));
            head.setItemMeta(skullMeta);
            return head;
        } else {
            return new ItemStack(Material.valueOf(material));
        }
    }


    public void registerCommandDynamic(String command, XKitsCommands cmd) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            PluginCommand pluginCommand = constructor.newInstance(command, playerKits);

            pluginCommand.setExecutor(cmd);
            pluginCommand.setTabCompleter(cmd);

            Field commandMapField = playerKits.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) commandMapField.get(playerKits.getServer());

            commandMap.register(command, pluginCommand);
        } catch (Exception e) {
        }
    }

}