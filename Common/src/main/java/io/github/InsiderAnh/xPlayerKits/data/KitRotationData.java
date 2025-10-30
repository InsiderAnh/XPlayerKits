package io.github.InsiderAnh.xPlayerKits.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KitRotationData {

    private String kitName;
    private long rotatedAt;
    private long nextFinishCooldown;
    private boolean active;
    private int probability;

    public KitRotationData(String kitName, long rotatedAt, long nextFinishCooldown) {
        this.kitName = kitName;
        this.rotatedAt = rotatedAt;
        this.nextFinishCooldown = nextFinishCooldown;
        this.active = true;
        this.probability = 100;
    }

    public boolean isExpired() {
        if (nextFinishCooldown == -1) return false;
        return System.currentTimeMillis() >= nextFinishCooldown;
    }

    public boolean isActive() {
        return active && !isExpired();
    }

}

