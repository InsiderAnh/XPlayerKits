package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.menus.KitsRotationMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitsOpenArgument extends StellarArgument {

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        Player player = (Player) sender;
        if (arguments.length < 1) {
            player.sendMessage("§cUsage: §7/xkits open <rotation/category>");
            return;
        }
        playerKits.getDatabase().getPlayerData(player.getUniqueId(), player.getName()).thenAccept(playerKitData -> {
            switch (arguments[0].toLowerCase()) {
                case "rotation":
                    playerKits.getStellarTaskHook(() -> new KitsRotationMenu(player, playerKitData, 1).open()).runTask(player.getLocation());
                    break;
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

}