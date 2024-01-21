package io.github.InsiderAnh.xPlayerKits.data;

import lombok.Data;

import java.util.HashMap;
import java.util.UUID;

@Data
public class PlayerKitData {

    private final UUID uuid;
    private final String name;
    private final HashMap<String, KitData> kitsData = new HashMap<>();

    @Override
    public String toString() {
        return "PlayerKitData{" +
            "uuid=" + uuid +
            ", name='" + name + '\'' +
            ", kitsData=" + kitsData +
            '}';
    }
}