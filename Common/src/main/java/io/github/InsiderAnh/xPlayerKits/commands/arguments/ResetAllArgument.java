package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ResetAllArgument extends StellarArgument {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (arguments.length < 1) {
            sendHelp(sender);
            return;
        }
        if (!sender.hasPermission("xkits.admin")) {
            sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }
        playerKits.getDatabase().getPlayerDataByName(arguments[0]).thenAccept(playerKitData -> {
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
    }

}