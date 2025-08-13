package io.github.InsiderAnh.xPlayerKits.commands;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class StellarCompleter {

    protected final PlayerKits playerKits = PlayerKits.getInstance();

    public abstract List<String> onTabComplete(@NotNull CommandSender sender, String[] arguments);

}