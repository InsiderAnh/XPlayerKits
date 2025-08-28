package io.github.InsiderAnh.xPlayerKits.commands;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.arguments.*;
import io.github.InsiderAnh.xPlayerKits.commands.completers.*;
import io.github.InsiderAnh.xPlayerKits.data.CountdownPlayer;
import io.github.InsiderAnh.xPlayerKits.menus.KitsMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XKitsCommands implements TabExecutor {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final HashMap<String, StellarArgument> arguments = new HashMap<>();
    private final HashMap<String, StellarCompleter> completers = new HashMap<>();

    public XKitsCommands() {
        arguments.put("editor", new EditorArgument());
        arguments.put("give", new GiveArgument());
        arguments.put("delete", new DeleteArgument());
        arguments.put("reset", new ResetArgument());
        arguments.put("resetall", new ResetAllArgument());
        arguments.put("preview", new PreviewArgument());

        arguments.put("migrate", new MigrateArgument());
        arguments.put("migratekits", new MigrateKitsArgument());

        arguments.put("reload", new ReloadArgument());

        arguments.put("kits", new KitsArgument());
        arguments.put("claim", new ClaimArgument());
        arguments.put("open", new KitsOpenArgument());

        completers.put("preview", new PreviewCompleter());
        completers.put("give", new GiveCompleter());
        completers.put("delete", new DeleteCompleter());
        completers.put("resetall", new ResetAllCompleter());
        completers.put("migrate", new MigrateCompleter());
        completers.put("migratekits", new MigrateKitsCompleter());
        completers.put("open", new KitsOpenCompleter());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            CountdownPlayer countdownPlayer = CountdownPlayer.getCountdownPlayer(player);
            if (countdownPlayer.isCountdown("kitCommandCountdown")) {
                player.sendMessage(playerKits.getLang().getString("messages.pleaseWait"));
                return true;
            }
            countdownPlayer.setCountdown("kitCommandCountdown", 500, TimeUnit.MILLISECONDS);
            if (args.length < 1) {
                if (playerKits.getConfig().getBoolean("kitsCMD.enabled")) {
                    playerKits.getDatabase().getPlayerData(player.getUniqueId(), player.getName()).thenAccept(playerKitData -> {
                        playerKits.getStellarTaskHook(() -> {
                            new KitsMenu(player, playerKitData, 1).open();
                            countdownPlayer.resetCountdown("kitCommandCountdown");
                        }).runTask(player.getLocation());
                    }).exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
                } else {
                    sendHelp(sender);
                }
                return true;
            }
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "editor":
            case "migratekits":
            case "open":
            case "kits": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command is only for players.");
                    return true;
                }
                String argument = args[0].toLowerCase();
                arguments.get(argument).onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return false;
            }
            case "give":
            case "claim":
            case "delete":
            case "reset":
            case "resetall":
            case "migrate":
            case "preview":
            case "reload": {
                String argument = args[0].toLowerCase();
                arguments.get(argument).onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            }
            default:
                sendHelp(sender);
                break;
        }
        return false;
    }

    void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+---------------------------------------+");
        sender.sendMessage(ChatColor.DARK_GRAY + "[!] " + ChatColor.RED + "XPlayerKits " + ChatColor.DARK_GRAY + "[!]");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+---------------------------------------+");
        sender.sendMessage("§e/xkits editor §7- §fOpens the kit editor.");
        sender.sendMessage("§e/xkits kits §7- §fOpens the kit menu.");
        sender.sendMessage("§e/xkits open [rotation/category] §7- §fOpens the kit menu.");
        sender.sendMessage("§e/xkits give <kitName> <player> §7- §fDirectly give kits to players without verifications..");
        sender.sendMessage("§e/xkits claim <kitName> <player> §7- §fGive kits to players with verifications.");
        sender.sendMessage("§e/xkits delete <kitName> §7- §fDelete a kit.");
        sender.sendMessage("§e/xkits preview <kitName> §7- §fPreview a kit.");
        sender.sendMessage("§e/xkits reset <kitName> <player> §7- §fReset a certain kit data.");
        sender.sendMessage("§e/xkits resetall <player> §7- §fReset all kit data.");
        sender.sendMessage("§e/xkits migrate playerkits2_yml/playerkits2_mysql §7- §fMigrate data from playerkits2 plugin.");
        sender.sendMessage("§e/xkits migratekits playerkits2 §7- §fMigrate kit from playerkits2 plugin.");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+---------------------------------------+");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            String argument = args.length == 0 ? "" : args[0].toLowerCase();
            return Stream.of("editor", "slots", "kits", "give", "claim", "delete", "reset", "resetall", "migrate", "migratekits", "reload", "open").filter(s -> s.contains(argument)).collect(Collectors.toList());
        }
        switch (args[0].toLowerCase()) {
            case "give":
            case "claim":
            case "reset":
                return completers.get("give").onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            case "delete":
            case "preview":
            case "resetall":
            case "migrate":
            case "migratekits":
            case "open":
                return completers.get(args[0].toLowerCase()).onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            default:
                return null;
        }
    }

}