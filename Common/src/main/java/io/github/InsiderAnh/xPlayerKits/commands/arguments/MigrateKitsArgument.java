package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.managers.MigratorManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MigrateKitsArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        Player player = (Player) sender;
        if (arguments.length < 1) {
            sendHelp(sender);
            return;
        }
        if (!sender.hasPermission("xkits.admin")) {
            sender.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }
        switch (arguments[0].toLowerCase()) {
            case "playerkits2":
                new MigratorManager().migrateKitsFromPlayerKits2(player);
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

}