package io.github.InsiderAnh.xPlayerKits.executions.executions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;

public class ExecuteWaitTicks extends Execution {

    private final int ticks;

    public ExecuteWaitTicks(String action, int ticks) {
        super(action);
        this.ticks = ticks;
    }

    @Override
    public void execute(Player player, Placeholder... placeholders) {
    }

    @Override
    public void executeActions(List<Execution> executions, Player player, Placeholder... placeholders) {
        new BukkitRunnable() {
            @Override
            public void run() {
                LinkedList<Execution> list = new LinkedList<>(executions);
                for (Execution execution : new LinkedList<>(list)) {
                    list.remove(execution);
                    if (execution instanceof ExecuteWaitTicks) {
                        ExecuteWaitTicks waitTicks = (ExecuteWaitTicks) execution;
                        waitTicks.executeActions(list, player, placeholders);
                    } else {
                        execution.execute(player, placeholders);
                    }
                }
            }
        }.runTaskLater(PlayerKits.getInstance(), ticks);
    }

    public String getActionType() {
        return "wait_ticks";
    }

}