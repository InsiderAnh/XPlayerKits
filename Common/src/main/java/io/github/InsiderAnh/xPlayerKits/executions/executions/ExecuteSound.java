package io.github.InsiderAnh.xPlayerKits.executions.executions;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.executions.Execution;
import io.github.InsiderAnh.xPlayerKits.executions.enums.SoundType;
import io.github.InsiderAnh.xPlayerKits.placeholders.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ExecuteSound extends Execution {

    private final String sound;
    private final float volume;
    private final float pitch;
    private final String data;
    private final SoundType soundType;

    public ExecuteSound(SoundType soundType, String data) {
        this.soundType = soundType;
        this.data = data;
        String[] sep = data.split(";");
        sound = sep[0];
        volume = sep.length >= 2 ? Float.parseFloat(sep[1]) : 1.0f;
        pitch = sep.length >= 3 ? Float.parseFloat(sep[2]) : 1.0f;
    }

    @Override
    public void execute(Player player, Placeholder... placeholders) {
        String[] sep = data.split(";");

        Location location = null;
        if (sep.length >= 4) {
            String[] loc = sep[3].split(",");
            location = new Location(Bukkit.getWorld(loc[3]), Double.parseDouble(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2]));
        }

        if (soundType.equals(SoundType.NORMAL_SOUND)) {
            try {
                if (location != null) {
                    location.getWorld().playSound(location, Sound.valueOf(sound.toUpperCase()), volume, pitch);
                } else {
                    player.playSound(player.getLocation(), Sound.valueOf(sound.toUpperCase()), volume, pitch);
                }
            } catch (Exception exception) {
                PlayerKits.getInstance().getLogger().info("Invalid sound: " + sound);
            }
        } else {
            if (location != null) {
                PlayerKits.getInstance().getPlayerKitsNMS().playSound(location, sound, volume, pitch);
            } else {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }

            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

}
