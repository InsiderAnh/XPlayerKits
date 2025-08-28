package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.menus.setup.KitMainEditorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditorArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        Player player = (Player) sender;
        if (!player.hasPermission("xkits.admin")) {
            player.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }
        new KitMainEditorMenu(player, 1).open();
    }

}
