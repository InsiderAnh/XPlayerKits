package io.github.InsiderAnh.xPlayerKits.customize.actions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MenuActionArg extends MenuAction {

    private final String value;

    public MenuActionArg(String action, String value) {
        super(action);
        this.value = value;
    }

    @Override
    public void execute(Player player) {
        if (action.equals("command")) {
            player.chat(value.replace("<player>", player.getName()));
        }
        if (action.equals("console")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value.replace("<player>", player.getName()));
        }
        if (action.equals("message")) {
            player.sendMessage(PlayerKits.getInstance().getColorUtils().color(value.replace("<player>", player.getName())));
        }
    }

}