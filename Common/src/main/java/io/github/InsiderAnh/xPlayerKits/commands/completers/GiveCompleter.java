package io.github.InsiderAnh.xPlayerKits.commands.completers;

import io.github.InsiderAnh.xPlayerKits.commands.StellarCompleter;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GiveCompleter extends StellarCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] arguments) {
        if (arguments.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().contains(arguments[arguments.length - 1].toLowerCase())).collect(Collectors.toList());
        }
        return playerKits.getKitManager().getKits().values().stream().filter(Objects::nonNull).map(Kit::getName).collect(Collectors.toList());
    }

}