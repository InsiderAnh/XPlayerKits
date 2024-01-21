package io.github.InsiderAnh.xPlayerKits.commands;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.CountdownPlayer;
import io.github.InsiderAnh.xPlayerKits.menus.KitSlotEditorMenu;
import io.github.InsiderAnh.xPlayerKits.menus.KitsMenu;
import io.github.InsiderAnh.xPlayerKits.menus.MainKitEditorMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class XKitsCommands implements CommandExecutor {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            CountdownPlayer countdownPlayer = CountdownPlayer.getCountdownPlayer(player);
            if (countdownPlayer.isCountdown("kitCommandCountdown")) {
                player.sendMessage(playerKits.getLang().getString("messages.pleaseWait"));
                return true;
            }
            countdownPlayer.setCountdown("kitCommandCountdown", 1, TimeUnit.SECONDS);
            if (args.length < 1) {
                if (playerKits.getConfig().getBoolean("kitsCMD.enabled")) {
                    playerKits.getDatabase().getPlayerData(player.getUniqueId(), player.getName()).thenAccept(playerKitData -> {
                        Bukkit.getScheduler().runTask(playerKits, () -> {
                            new KitsMenu(player, playerKitData, 1).open();
                            countdownPlayer.resetCountdown("kitCommandCountdown");
                        });
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                } else {
                    sendHelp(sender);
                }
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "editor":
                    if (!player.hasPermission("xkits.admin")) {
                        player.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    new MainKitEditorMenu(player).open();
                    countdownPlayer.resetCountdown("kitCommandCountdown");
                    break;
                case "slots":
                    if (!player.hasPermission("xkits.admin")) {
                        player.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    new KitSlotEditorMenu(player).open();
                    countdownPlayer.resetCountdown("kitCommandCountdown");
                    break;
                case "kits":
                    playerKits.getDatabase().getPlayerData(player.getUniqueId(), player.getName()).thenAccept(playerKitData -> {
                        Bukkit.getScheduler().runTask(playerKits, () -> {
                            new KitsMenu(player, playerKitData, 1).open();
                            countdownPlayer.resetCountdown("kitCommandCountdown");
                        });
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        } else {
            sender.sendMessage("§cThis command is only usable by players.");
        }
        return false;
    }

    void sendHelp(CommandSender sender) {
        sender.sendMessage("§7§m--------------------------------------");
        sender.sendMessage("§e/xkits editor §7- §fOpens the kit editor.");
        sender.sendMessage("§e/xkits slots §7- §fOpens the kit slot editor.");
        sender.sendMessage("§e/xkits kits §7- §fOpens the kit menu.");
        sender.sendMessage("§7§m--------------------------------------");
    }

}