package io.github.InsiderAnh.xPlayerKits.data;

import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Setter
public class CountdownPlayer {

    private static HashMap<UUID, CountdownPlayer> countdownPlayerMap = new HashMap<>();
    private HashMap<String, Long> countdowns = new HashMap<>();

    public static CountdownPlayer getCountdownPlayer(Player player) {
        countdownPlayerMap.putIfAbsent(player.getUniqueId(), new CountdownPlayer());
        return countdownPlayerMap.get(player.getUniqueId());
    }

    public static void removeCountdownPlayer(Player player) {
        countdownPlayerMap.remove(player.getUniqueId());
    }

    public void resetCountdown(String countdownId) {
        countdowns.remove(countdownId);
    }

    public boolean isCountdown(String countdownId) {
        return countdowns.getOrDefault(countdownId, 0L) > System.currentTimeMillis();
    }

    public void setCountdown(String countdownId, int time, TimeUnit timeUnit) {
        countdowns.put(countdownId, System.currentTimeMillis() + timeUnit.toMillis(time));
    }

    public int getSeconds(String countdownId) {
        return (int) ((countdowns.get(countdownId) - System.currentTimeMillis()) / 1000) + 1;
    }

}