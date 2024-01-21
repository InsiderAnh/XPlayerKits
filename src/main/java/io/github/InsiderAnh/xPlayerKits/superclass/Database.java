package io.github.InsiderAnh.xPlayerKits.superclass;

import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Database {

    public HashMap<UUID, PlayerKitData> cachedPlayerKits = new HashMap<>();

    public abstract void connect();

    public abstract void close();

    public abstract CompletableFuture<PlayerKitData> getPlayerData(UUID uuid, String name);

    public abstract void updatePlayerData(UUID uuid);

    public void removePlayerData(UUID uuid) {
        cachedPlayerKits.remove(uuid);
    }

}