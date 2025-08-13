package io.github.InsiderAnh.xPlayerKits.commands.completers;

import io.github.InsiderAnh.xPlayerKits.commands.StellarCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class ResetAllCompleter extends StellarCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] arguments) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().contains(arguments[arguments.length - 1].toLowerCase())).collect(Collectors.toList());
    }

}
