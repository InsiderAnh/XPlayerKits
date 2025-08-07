package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import io.github.InsiderAnh.xPlayerKits.menus.KitsMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitsArgument extends StellarArgument {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        Player player = (Player) sender;
        playerKits.getDatabase().getPlayerData(player.getUniqueId(), player.getName()).thenAccept(playerKitData -> {
            Bukkit.getScheduler().runTask(playerKits, () -> new KitsMenu(player, playerKitData, 1).open());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });
    }

}