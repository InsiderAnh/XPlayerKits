package io.github.InsiderAnh.xPlayerKits.listeners;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.CountdownPlayer;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerKits.getDatabase().loadPlayerData(player.getUniqueId(), player.getName()).thenAccept(firstJoin -> {
            if (!firstJoin || !playerKits.getConfigManager().isKitOnJoin()) return;

            Kit kit = playerKits.getKitManager().getKits().get(playerKits.getConfigManager().getKitOnJoinName());
            if (kit == null) return;

            Bukkit.getScheduler().runTask(playerKits, () -> kit.giveKit(player));
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerKits.getDatabase().removePlayerData(player.getUniqueId());
        CountdownPlayer.removeCountdownPlayer(player);
    }

}