package io.github.InsiderAnh.xPlayerKits.kits.properties;

import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class PropertyTiming {

    private long countdown;
    private boolean oneTime;

    private boolean rotationEnabled = false;
    private long rotationTime = 0;
    private long rotationCooldown = 0;
    private int rotationProbability = -1;

    public PropertyTiming(InsiderConfig config) {
        if (config.isSet("timing")) {
            this.countdown = config.getConfig().getLong("timing.countdown");
            this.oneTime = config.getConfig().getBoolean("timing.oneTime");
            this.rotationEnabled = config.getConfig().getBoolean("timing.rotationEnabled");
            this.rotationTime = config.getConfig().getLong("timing.rotationTime");
            this.rotationCooldown = config.getConfig().getLong("timing.rotationCooldown");
            this.rotationProbability = config.getConfig().getInt("timing.rotationProbability");
        } else {
            this.countdown = config.getLong("countdown");
            this.oneTime = config.getBoolean("oneTime");
        }
    }

    public PropertyTiming() {
        this.countdown = TimeUnit.MINUTES.toSeconds(5);
        this.oneTime = false;
        this.rotationEnabled = false;
        this.rotationTime = 0;
        this.rotationCooldown = 0;
        this.rotationProbability = -1;
    }

    public void save(InsiderConfig config) {
        config.set("countdown", null);
        config.set("oneTime", null);

        config.set("timing.countdown", countdown);
        config.set("timing.oneTime", oneTime);
        config.set("timing.rotationEnabled", rotationEnabled);
        config.set("timing.rotationTime", rotationTime);
        config.set("timing.rotationCooldown", rotationCooldown);
        config.set("timing.rotationProbability", rotationProbability);
    }

}