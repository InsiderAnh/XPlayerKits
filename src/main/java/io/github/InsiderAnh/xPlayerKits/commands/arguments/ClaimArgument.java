package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimArgument extends StellarArgument {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (arguments.length < 2) {
            sendHelp(sender);
            return;
        }
        if (!sender.hasPermission("xkits.admin")) {
            sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }
        Kit kit = playerKits.getKitManager().getKit(arguments[0]);
        Player online = Bukkit.getPlayer(arguments[1]);
        if (kit == null) {
            sender.sendMessage("§cThis kit don´t exists.");
            return;
        }
        if (online == null) {
            sender.sendMessage("§cThis player is not online.");
            return;
        }
        playerKits.getDatabase().getPlayerData(online.getUniqueId(), online.getName()).thenAccept(playerKitData -> {
            Bukkit.getScheduler().runTask(playerKits, () -> {
                XPKUtils.claimKit(online, kit, playerKitData);
            });
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

}