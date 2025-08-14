package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.menus.KitPreviewMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PreviewArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (arguments.length < 1) {
            sendHelp(sender);
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("xkits.preview")) {
                player.sendMessage(playerKits.getLang().getString("messages.noPermission"));
                return;
            }
            Kit kit = playerKits.getKitManager().getKit(arguments[0]);
            Player target = arguments.length == 2 ? Bukkit.getPlayer(arguments[1]) : player;
            if (target == null) {
                sender.sendMessage("§cThis player is not online.");
                return;
            }

            new KitPreviewMenu(target, kit).open();
            return;
        }
        if (arguments.length < 2) {
            sendHelp(sender);
            return;
        }
        Player target = Bukkit.getPlayer(arguments[1]);
        if (target == null) {
            sender.sendMessage("§cThis player is not online.");
            return;
        }
        Kit kit = playerKits.getKitManager().getKit(arguments[0]);
        new KitPreviewMenu(target, kit).open();
    }

}