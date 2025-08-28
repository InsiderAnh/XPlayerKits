package io.github.InsiderAnh.xPlayerKits.executions.executions;

import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.executions.enums.CommandType;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import io.github.InsiderAnh.xPlayerKits.utils.LanguageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExecuteCommand extends Execution {

    private final CommandType commandType;
    private final String command;

    public ExecuteCommand(CommandType commandType, String command) {
        this.commandType = commandType;
        this.command = command;
    }

    @Override
    public void execute(Player player, Placeholder... placeholders) {
        String replacedCommand = LanguageUtils.replacePlaceholders(this.command, placeholders);

        if (commandType.equals(CommandType.PLAYER_COMMAND)) {
            player.performCommand(replacedCommand.replace("%player%", player.getName()));
        } else if (commandType.equals(CommandType.CONSOLE_COMMAND)) {
            player.getServer().dispatchCommand(Bukkit.getConsoleSender(), replacedCommand.replace("%player%", player.getName()));
        }
    }

}
