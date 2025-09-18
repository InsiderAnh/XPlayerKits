package io.github.InsiderAnh.xPlayerKits.api;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class XKitsAPI {

    private static final PlayerKits plugin = PlayerKits.getInstance();

    public static List<Kit> getKits() {
        return plugin.getKitManager().getKits().values().stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
    }

}