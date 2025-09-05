package io.github.InsiderAnh.xPlayerKits.api.events;

import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
public class ClaimXKitEvent extends XKitEvent implements Cancellable {

    private final Player player;
    private final Kit kit;
    private final String kitName;
    private boolean cancelled;

    public ClaimXKitEvent(Player player, Kit kit) {
        this.player = player;
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