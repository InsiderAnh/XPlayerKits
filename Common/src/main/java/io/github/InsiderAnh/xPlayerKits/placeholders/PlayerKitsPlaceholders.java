package io.github.InsiderAnh.xPlayerKits.placeholders;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
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

        if (placeholder.startsWith("cooldown_")) {
            return parseCountdown(getKitFromPlaceholder(placeholder, "cooldown_"), playerKitData);
        }
        if (placeholder.startsWith("claimed_onetime_")) {
            return parseClaimed(getKitFromPlaceholder(placeholder, "claimed_onetime_"), playerKitData);
        }
        if (placeholder.startsWith("claimed_cooldown_")) {
            return parseCooldown(getKitFromPlaceholder(placeholder, "claimed_cooldown_"), playerKitData);
        }

        return null;
    }

    private String parseCountdown(Kit kit, PlayerKitData playerKitData) {
        if (kit == null) return "No such kit";

        KitData kitData = playerKitData.getKitsData().get(kit.getName());
        if (kitData == null || kitData.getCountdown() <= System.currentTimeMillis()) {
            return playerKits.getLang().getString("countdown.noCountdown");
        }

        long remaining = kitData.getCountdown() - System.currentTimeMillis();
        return XPKUtils.millisToLongDHMS(remaining);
    }

    private String parseClaimed(Kit kit, PlayerKitData playerKitData) {
        if (kit == null) return "No such kit";

        KitData kitData = playerKitData.getKitsData().get(kit.getName());
        return (kitData != null && kitData.isBought()) ? "yes" : "no";
    }

    private String parseCooldown(Kit kit, PlayerKitData playerKitData) {
        if (kit == null) return "No such kit";

        KitData kitData = playerKitData.getKitsData().get(kit.getName());
        return (kitData != null && kitData.getCountdown() > System.currentTimeMillis()) ? "yes" : "no";
    }

    private Kit getKitFromPlaceholder(String placeholder, String prefix) {
        String kitName = placeholder.substring(prefix.length());
        return playerKits.getKitManager().getKit(kitName);
    }

}