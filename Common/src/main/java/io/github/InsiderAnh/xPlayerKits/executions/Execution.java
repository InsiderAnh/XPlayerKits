package io.github.InsiderAnh.xPlayerKits.executions;

import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public abstract class Execution {

    public final String action;

    protected Execution(String action) {
        this.action = action;
    }

    public abstract void execute(Player player, Placeholder... placeholders);

    public void executeActions(List<Execution> executions, Player player, Placeholder... placeholders) {

    }

}