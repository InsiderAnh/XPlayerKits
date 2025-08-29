package io.github.InsiderAnh.xPlayerKits.commands.completers;

import io.github.InsiderAnh.xPlayerKits.commands.StellarCompleter;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeleteCompleter extends StellarCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] arguments) {
        String filter = arguments.length == 1 ? arguments[0] : "";
        return playerKits.getKitManager().getKits().values().stream().filter(Objects::nonNull).map(Kit::getName).filter(kitName -> filter.isEmpty() || kitName.toLowerCase().contains(filter.toLowerCase())).collect(Collectors.toList());
    }

}