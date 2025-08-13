package io.github.InsiderAnh.xPlayerKits.commands.completers;

import io.github.InsiderAnh.xPlayerKits.commands.StellarCompleter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MigrateCompleter extends StellarCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, String[] arguments) {
        return new ArrayList<>(Arrays.asList("playerkits2_yml", "playerkits2_mysql"));
    }

}