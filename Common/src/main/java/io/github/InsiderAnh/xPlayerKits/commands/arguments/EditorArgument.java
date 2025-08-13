package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.menus.setup.MainKitEditorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditorArgument extends StellarArgument {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        Player player = (Player) sender;
        if (!player.hasPermission("xkits.admin")) {
            player.sendMessage(playerKits.getLang().getString("messages.noPermission"));
            return;
        }
        new MainKitEditorMenu(player, 1).open();
    }

}
