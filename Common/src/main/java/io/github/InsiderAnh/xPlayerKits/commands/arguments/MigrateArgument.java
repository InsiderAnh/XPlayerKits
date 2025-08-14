package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.managers.MigratorManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MigrateArgument extends StellarArgument {

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
        switch (arguments[0].toLowerCase()) {
            case "playerkits2_yml":
                new MigratorManager().migrateFromPlayerKits2Yaml();
                break;
            case "playerkits2_mysql":
                new MigratorManager().migrateFromPlayerKits2MySQL();
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

}