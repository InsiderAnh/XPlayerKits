package io.github.InsiderAnh.xPlayerKits.commands;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.arguments.*;
import io.github.InsiderAnh.xPlayerKits.data.CountdownPlayer;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.menus.KitsMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class XKitsCommands implements TabExecutor {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final HashMap<String, StellarArgument> arguments = new HashMap<>();
    private final HashMap<String, StellarCompleter> completers = new HashMap<>();

    public XKitsCommands() {
        arguments.put("editor", new EditorArgument());
        arguments.put("slots", new SlotsArgument());
        arguments.put("give", new GiveArgument());
        arguments.put("delete", new DeleteArgument());
        arguments.put("reset", new ResetArgument());
        arguments.put("resetall", new ResetAllArgument());

        arguments.put("migrate", new MigrateArgument());
        arguments.put("migratekits", new MigrateKitsArgument());

        arguments.put("reload", new ReloadArgument());

        arguments.put("kits", new KitsArgument());
        arguments.put("claim", new ClaimArgument());
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
            case "slots":
            case "migratekits":
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return new ArrayList<>(Arrays.asList("editor", "slots", "kits", "give", "claim", "delete", "reset", "resetall", "migrate", "migratekits", "reload"));
        }
        switch (args[0].toLowerCase()) {
            case "give":
            case "claim":
            case "reset":
                if (args.length == 3) {
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().contains(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
                }
                return playerKits.getKitManager().getKits().values().stream().map(Kit::getName).collect(Collectors.toList());
            case "delete":
                return playerKits.getKitManager().getKits().values().stream().map(Kit::getName).collect(Collectors.toList());
            case "resetall":
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().contains(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
            case "migrate":
                return new ArrayList<>(Arrays.asList("playerkits2_yml", "playerkits2_mysql"));
            case "migratekits":
                return new ArrayList<>(Arrays.asList("playerkits2"));
            default:
                return null;
        }
    }

}