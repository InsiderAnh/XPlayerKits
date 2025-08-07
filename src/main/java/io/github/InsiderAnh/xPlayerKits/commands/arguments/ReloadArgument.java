package io.github.InsiderAnh.xPlayerKits.commands.arguments;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.commands.StellarArgument;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadArgument extends StellarArgument {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public void onCommand(@NotNull CommandSender sender, String[] arguments) {
        playerKits.reload();
        sender.sendMessage("§aPlugin reloaded correctly.");
    }

}