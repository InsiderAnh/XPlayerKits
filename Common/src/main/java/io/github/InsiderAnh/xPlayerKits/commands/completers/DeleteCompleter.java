package io.github.InsiderAnh.xPlayerKits.commands.completers;

import io.github.InsiderAnh.xPlayerKits.commands.StellarCompleter;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class DeleteCompleter extends StellarCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] arguments) {
        return playerKits.getKitManager().getKits().values().stream().map(Kit::getName).collect(Collectors.toList());
    }

}