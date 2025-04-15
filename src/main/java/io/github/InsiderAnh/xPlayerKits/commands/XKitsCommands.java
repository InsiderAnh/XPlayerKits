package io.github.InsiderAnh.xPlayerKits.commands;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.CountdownPlayer;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.managers.MigratorManager;
import io.github.InsiderAnh.xPlayerKits.menus.KitSlotEditorMenu;
import io.github.InsiderAnh.xPlayerKits.menus.KitsMenu;
import io.github.InsiderAnh.xPlayerKits.menus.setup.MainKitEditorMenu;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
                    new MainKitEditorMenu(player, 1).open();
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
                case "give": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().getKit(args[1]);
                    Player online = Bukkit.getPlayer(args[2]);
                    if (kit == null) {
                        sender.sendMessage("§cThis kit don´t exists.");
                        return true;
                    }
                    if (online == null) {
                        sender.sendMessage("§cThis player is not online.");
                        return true;
                    }
                    kit.giveKit(online);
                    break;
                }
                case "delete": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().removeKit(args[1]);
                    if (kit != null) {
                        File fileKit = new File(playerKits.getDataFolder(), kit.getName() + ".yml");
                        if (fileKit.exists()) {
                            fileKit.delete();
                        }
                        sender.sendMessage(playerKits.getLang().getString("messages.deletedKit"));
                    } else {
                        sender.sendMessage(playerKits.getLang().getString("messages.noExistsKit"));
                    }
                    break;
                }
                case "reset": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().getKit(args[1]);
                    if (kit == null) {
                        sender.sendMessage("§cThis kit don´t exists.");
                        return true;
                    }
                    playerKits.getDatabase().getPlayerDataByName(args[2]).thenAccept(playerKitData -> {
                        if (playerKitData == null) {
                            sender.sendMessage("§cThis user don´t have data.");
                            return;
                        }
                        playerKitData.getKitsData().remove(kit.getName());
                        playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                }
                case "resetall": {
                    if (args.length < 2) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    playerKits.getDatabase().getPlayerDataByName(args[1]).thenAccept(playerKitData -> {
                        if (playerKitData == null) {
                            sender.sendMessage("§cThis user don´t have data.");
                            return;
                        }
                        playerKitData.getKitsData().clear();
                        playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                }
                case "migrate":
                    if (args.length < 2) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    switch (args[1].toLowerCase()) {
                        case "playerkits2_yml":
                            new MigratorManager().migrateFromPlayerKits2MoreOptimized();
                            break;
                        case "playerkits2_mysql":
                            new MigratorManager().migrateFromPlayerKits2MySQL();
                            break;
                        default:
                            sendHelp(sender);
                            break;
                    }
                    break;
                case "migratekits":
                    if (args.length < 2) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    switch (args[1].toLowerCase()) {
                        case "playerkits2":
                            new MigratorManager().migrateKitsFromPlayerKits2(player);
                            break;
                        default:
                            sendHelp(sender);
                            break;
                    }
                    break;
                case "claim": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().getKit(args[1]);
                    Player online = Bukkit.getPlayer(args[2]);
                    if (kit == null) {
                        sender.sendMessage("§cThis kit don´t exists.");
                        return true;
                    }
                    if (online == null) {
                        sender.sendMessage("§cThis player is not online.");
                        return true;
                    }
                    playerKits.getDatabase().getPlayerData(online.getUniqueId(), online.getName()).thenAccept(playerKitData -> {
                        Bukkit.getScheduler().runTask(playerKits, () -> {
                            XPKUtils.claimKit(online, kit, playerKitData);
                        });
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                }
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
                case "reload":
                    playerKits.reload();
                    sender.sendMessage("§aPlugin reloaded correctly.");
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        } else {
            if (args.length < 1) {
                sendHelp(sender);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "give": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().getKit(args[1]);
                    Player online = Bukkit.getPlayer(args[2]);
                    if (kit == null) {
                        sender.sendMessage("§cThis kit don´t exists.");
                        return true;
                    }
                    if (online == null) {
                        sender.sendMessage("§cThis player is not online.");
                        return true;
                    }
                    kit.giveKit(online);
                    break;
                }
                case "claim": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().getKit(args[1]);
                    Player online = Bukkit.getPlayer(args[2]);
                    if (kit == null) {
                        sender.sendMessage("§cThis kit don´t exists.");
                        return true;
                    }
                    if (online == null) {
                        sender.sendMessage("§cThis player is not online.");
                        return true;
                    }
                    playerKits.getDatabase().getPlayerData(online.getUniqueId(), online.getName()).thenAccept(playerKitData -> {
                        Bukkit.getScheduler().runTask(playerKits, () -> {
                            XPKUtils.claimKit(online, kit, playerKitData);
                        });
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                }
                case "delete": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().removeKit(args[1]);
                    if (kit != null) {
                        File fileKit = new File(playerKits.getDataFolder(), kit.getName() + ".yml");
                        if (fileKit.exists()) {
                            fileKit.delete();
                        }
                        sender.sendMessage(playerKits.getLang().getString("messages.deletedKit"));
                    } else {
                        sender.sendMessage(playerKits.getLang().getString("messages.noExistsKit"));
                    }
                    break;
                }
                case "reset": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    Kit kit = playerKits.getKitManager().getKit(args[1]);
                    if (kit == null) {
                        sender.sendMessage("§cThis kit don´t exists.");
                        return true;
                    }
                    playerKits.getDatabase().getPlayerDataByName(args[2]).thenAccept(playerKitData -> {
                        if (playerKitData == null) {
                            sender.sendMessage("§cThis user don´t have data.");
                            return;
                        }
                        playerKitData.getKitsData().remove(kit.getName());
                        playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                }
                case "resetall": {
                    if (args.length < 3) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    playerKits.getDatabase().getPlayerDataByName(args[1]).thenAccept(playerKitData -> {
                        if (playerKitData == null) {
                            sender.sendMessage("§cThis user don´t have data.");
                            return;
                        }
                        playerKitData.getKitsData().clear();
                        playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                    break;
                }
                case "migrate":
                    if (args.length < 2) {
                        sendHelp(sender);
                        return true;
                    }
                    if (!sender.hasPermission("xkits.admin")) {
                        sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                        return true;
                    }
                    switch (args[1].toLowerCase()) {
                        case "playerkits2_yml":
                            new MigratorManager().migrateFromPlayerKits2MoreOptimized();
                            break;
                        case "playerkits2_mysql":
                            new MigratorManager().migrateFromPlayerKits2MySQL();
                            break;
                        default:
                            sendHelp(sender);
                            break;
                    }
                    break;
                case "reload":
                    playerKits.reload();
                    sender.sendMessage("§aPlugin reloaded correctly.");
                    break;
                default:
                    sendHelp(sender);
                    break;
            }
        }
        return false;
    }

    void sendHelp(CommandSender sender) {
        sender.sendMessage("§7§m--------------------------------------");
        sender.sendMessage("§e/xkits editor §7- §fOpens the kit editor.");
        sender.sendMessage("§e/xkits slots §7- §fOpens the kit slot editor.");
        sender.sendMessage("§e/xkits kits §7- §fOpens the kit menu.");
        sender.sendMessage("§e/xkits give <kitName> <player> §7- §fDirectly give kits to players without verifications..");
        sender.sendMessage("§e/xkits claim <kitName> <player> §7- §fGive kits to players with verifications.");
        sender.sendMessage("§e/xkits delete <kitName> §7- §fDelete a kit.");
        sender.sendMessage("§e/xkits reset <kitName> <player> §7- §fReset a certain kit data.");
        sender.sendMessage("§e/xkits resetall <player> §7- §fReset all kit data.");
        sender.sendMessage("§e/xkits migrate playerkits2_yml/playerkits2_mysql §7- §fMigrate data from playerkits2 plugin.");
        sender.sendMessage("§e/xkits migratekits playerkits2 §7- §fMigrate kit from playerkits2 plugin.");
        sender.sendMessage("§7§m--------------------------------------");
    }

}