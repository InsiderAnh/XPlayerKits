package io.github.InsiderAnh.xPlayerKits.placeholders;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerKitsPlaceholders extends PlaceholderExpansion {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @Override
    public @NotNull String getIdentifier() {
        return "xplayerkits";
    }

    @Override
    public @NotNull String getAuthor() {
        return "InsiderAnh";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        PlayerKitData playerKitData = playerKits.getDatabase().getCachedPlayerData(player.getUniqueId());
        if (placeholder.startsWith("cooldown")) {
            Kit kit = playerKits.getKitManager().getKit(placeholder.replace("cooldown_", ""));
            if (kit == null) {
                return "No exits kit";
            }
            if (playerKitData.getKitsData().containsKey(kit.getName())) {
                long millis = playerKitData.getKitsData().get(kit.getName()).getCountdown();
                if (millis > System.currentTimeMillis()) {
                    return XPKUtils.millisToLongDHMS(millis - System.currentTimeMillis());
                } else {
                    return playerKits.getLang().getString("countdown.noCountdown");
                }
            } else {
                return playerKits.getLang().getString("countdown.noCountdown");
            }
        }
        return null;
    }
}