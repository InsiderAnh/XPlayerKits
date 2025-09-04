package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import lombok.Data;

@Data
public class ConfigManager {

    private boolean checkUpdates;
    private boolean kitOnJoin;
    private boolean shortKit;
    private boolean claimAlias;
    private boolean kitsCMDEnabled;
    private String kitOnJoinName;

    public void load() {
        PlayerKits playerKits = PlayerKits.getInstance();
        this.checkUpdates = playerKits.getConfig().getBoolean("check-updates");
        this.kitsCMDEnabled = playerKits.getConfig().getBoolean("kitsCMD.enabled");
        this.shortKit = playerKits.getConfig().getBoolean("kitsCMD.shortKit");
        this.claimAlias = playerKits.getConfig().getBoolean("kitsCMD.claimAlias");
        this.kitOnJoin = playerKits.getConfig().getBoolean("kitOnJoin.enabled");
        this.kitOnJoinName = playerKits.getConfig().getString("kitOnJoin.kitName");
    }

}