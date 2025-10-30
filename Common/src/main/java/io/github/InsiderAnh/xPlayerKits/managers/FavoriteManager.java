package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.KitData;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class FavoriteManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();

    public boolean toggleFavorite(PlayerKitData playerKitData, String kitName) {
        KitData kitData = playerKitData.getKitsData().get(kitName);

        if (kitData == null) {
            kitData = new KitData(kitName, 0L, false, false, false, 0);
            playerKitData.getKitsData().put(kitName, kitData);
        }

        kitData.setFavorite(!kitData.isFavorite());
        playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());

        return kitData.isFavorite();
    }

    public boolean isFavorite(PlayerKitData playerKitData, String kitName) {
        KitData kitData = playerKitData.getKitsData().get(kitName);
        return kitData != null && kitData.isFavorite();
    }

    public List<Kit> getFavoriteKits(PlayerKitData playerKitData) {
        return playerKitData.getKitsData().values().stream()
                .filter(KitData::isFavorite)
                .map(kitData -> playerKits.getKitManager().getKit(kitData.getKitName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int getFavoriteCount(PlayerKitData playerKitData) {
        return (int) playerKitData.getKitsData().values().stream()
                .filter(KitData::isFavorite)
                .count();
    }

    public void removeFavorite(PlayerKitData playerKitData, String kitName) {
        KitData kitData = playerKitData.getKitsData().get(kitName);
        if (kitData != null) {
            kitData.setFavorite(false);
            playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
        }
    }

    public void clearFavorites(PlayerKitData playerKitData) {
        playerKitData.getKitsData().values().forEach(kitData -> kitData.setFavorite(false));
        playerKits.getDatabase().updatePlayerData(playerKitData.getUuid());
    }

}

