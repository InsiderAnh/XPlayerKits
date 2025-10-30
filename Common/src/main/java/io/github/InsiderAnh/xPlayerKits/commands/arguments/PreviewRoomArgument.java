package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.menus.PreviewRoomMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PreviewRoomArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command is only for players.");
            return;
        }

        Player player = (Player) sender;

        playerKits.getStellarTaskHook(() -> new PreviewRoomMenu(player, 1).open()).runTask(player.getLocation());
    }

}

