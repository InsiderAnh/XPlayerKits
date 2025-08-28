package io.github.InsiderAnh.xPlayerKits.customize.actions;

import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class MenuAction {

    protected final String action;

    public MenuAction(String action) {
        this.action = action;
    }

    public void execute(Player player) {
        if (action.equals("close_menu")) {
            player.closeInventory();
        }
    }

}