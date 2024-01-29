package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import lombok.Data;

@Data
public class ConfigManager {

    private boolean kitOnJoin;
    private String kitOnJoinName;

    public void load() {
        PlayerKits playerKits = PlayerKits.getInstance();
        this.kitOnJoin = playerKits.getConfig().getBoolean("kitOnJoin.enabled");
        this.kitOnJoinName = playerKits.getConfig().getString("kitOnJoin.kitName");
    }

}