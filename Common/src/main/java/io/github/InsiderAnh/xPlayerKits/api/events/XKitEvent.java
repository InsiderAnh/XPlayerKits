package io.github.InsiderAnh.xPlayerKits.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class XKitEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public XKitEvent(boolean isAsync) {
        super(isAsync);
    }

    public XKitEvent() {
        super(false);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

}
