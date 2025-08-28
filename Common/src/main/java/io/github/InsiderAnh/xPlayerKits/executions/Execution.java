package io.github.InsiderAnh.xPlayerKits.executions;

import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class Execution {

    public abstract void execute(Player player, Placeholder... placeholders);

    public void executeActions(List<Execution> executions, Player player, Placeholder... placeholders) {

    }

}