package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ResetArgument extends StellarArgument {

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
        if (kit == null) {
            sender.sendMessage("§cThis kit don´t exists.");
            return;
        }
        playerKits.getDatabase().getPlayerDataByName(arguments[1]).thenAccept(playerKitData -> {
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
    }

}