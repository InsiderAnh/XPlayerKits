package io.github.InsiderAnh.xPlayerKits.api.events;

import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
public class GiveXKitEvent extends XKitEvent implements Cancellable {

    private final CommandSender giver;
    private final Player receiver;
    private final Kit kit;
    private final String kitName;
    private boolean cancelled;

    public GiveXKitEvent(CommandSender giver, Player receiver, Kit kit) {
        this.giver = giver;
        this.receiver = receiver;
        this.kit = kit;
        this.kitName = kit.getName();
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}