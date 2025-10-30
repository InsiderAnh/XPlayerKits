package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RotationArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (!sender.hasPermission("xkits.admin")) {
            sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }

        if (arguments.length < 1) {
            sender.sendMessage("§cUsage: §7/xkits rotation <start|stop|check|force|list> [kitName]");
            return;
        }

        String action = arguments[0].toLowerCase();

        switch (action) {
            case "start":
                if (arguments.length < 2) {
                    sender.sendMessage("§cUsage: §7/xkits rotation start <kitName>");
                    return;
                }
                Kit kit = playerKits.getKitManager().getKit(arguments[1]);
                if (kit == null) {
                    sender.sendMessage(playerKits.getLang().getString("messages.noExistsKit"));
                    return;
                }
                if (playerKits.getRotationManager().activateRotation(kit)) {
                    sender.sendMessage("§aRotation activated for kit: §e" + kit.getName());
                } else {
                    sender.sendMessage("§cCouldn't activate rotation. Check if rotation is enabled and there's space.");
                }
                break;

            case "stop":
                if (arguments.length < 2) {
                    sender.sendMessage("§cUsage: §7/xkits rotation stop <kitName>");
                    return;
                }
                if (playerKits.getRotationManager().deactivateRotation(arguments[1])) {
                    sender.sendMessage("§aRotation stopped for kit: §e" + arguments[1]);
                } else {
                    sender.sendMessage("§cKit not found in rotation.");
                }
                break;

            case "check":
                playerKits.getRotationManager().checkRotations();
                sender.sendMessage("§aRotation check completed and updated.");
                break;

            case "force":
                playerKits.getRotationManager().forceRotate();
                sender.sendMessage("§aForced rotation refresh. All rotations cleared and new ones started.");
                break;

            case "list":
                List<Kit> activeKits = playerKits.getRotationManager().getActiveRotationKits();
                if (activeKits.isEmpty()) {
                    sender.sendMessage("§7No kits currently in rotation.");
                } else {
                    sender.sendMessage("§eActive rotation kits:");
                    for (Kit rotKit : activeKits) {
                        long timeRemaining = playerKits.getRotationManager().getTimeRemaining(rotKit.getName());
                        String timeStr = timeRemaining == -1 ? "Permanent" : formatTime(timeRemaining);
                        sender.sendMessage("§7- §f" + rotKit.getName() + " §7(§e" + timeStr + "§7)");
                    }
                }
                break;

            default:
                sender.sendMessage("§cUsage: §7/xkits rotation <start|stop|check|force|list> [kitName]");
                break;
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

}

