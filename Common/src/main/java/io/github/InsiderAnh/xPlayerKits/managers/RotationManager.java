package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.data.KitRotationData;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import io.github.InsiderAnh.xPlayerKits.kits.properties.PropertyTiming;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class RotationManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Map<String, KitRotationData> activeRotations = new ConcurrentHashMap<>();
    private InsiderConfig rotationConfig;
    private boolean rotationEnabled;
    private int maxActiveRotations;
    private long rotationCheckInterval;
    private long lastRotationCheck;

    public RotationManager() {
        this.rotationConfig = new InsiderConfig(playerKits, "rotation", true, false);
        load();
    }

    public void load() {
        this.rotationEnabled = rotationConfig.getBooleanOrDefault("enabled", true);
        this.maxActiveRotations = rotationConfig.getIntOrDefault("maxActiveRotations", 3);
        this.rotationCheckInterval = rotationConfig.getLongOrDefault("rotationCheckInterval", 60000L); // 1 minuto
        this.lastRotationCheck = rotationConfig.getLongOrDefault("lastRotationCheck", 0L);

        // Cargar rotaciones activas
        if (rotationConfig.isSet("rotations")) {
            for (String key : rotationConfig.getConfig().getConfigurationSection("rotations").getKeys(false)) {
                long rotatedAt = rotationConfig.getLongOrDefault("rotations." + key + ".rotated_at", 0L);
                long nextFinishCooldown = rotationConfig.getLongOrDefault("rotations." + key + ".next_finish_cooldown", -1L);
                String kitName = rotationConfig.getString("rotations." + key + ".kitId");
                boolean active = rotationConfig.getBooleanOrDefault("rotations." + key + ".active", true);
                int probability = rotationConfig.getIntOrDefault("rotations." + key + ".probability", 100);

                if (kitName != null && !kitName.isEmpty()) {
                    KitRotationData rotationData = new KitRotationData(kitName, rotatedAt, nextFinishCooldown);
                    rotationData.setActive(active);
                    rotationData.setProbability(probability);
                    activeRotations.put(kitName, rotationData);
                }
            }
        }

        // Verificar y actualizar rotaciones al cargar
        checkRotations();
    }

    public void save() {
        rotationConfig.set("enabled", rotationEnabled);
        rotationConfig.set("maxActiveRotations", maxActiveRotations);
        rotationConfig.set("rotationCheckInterval", rotationCheckInterval);
        rotationConfig.set("lastRotationCheck", lastRotationCheck);

        // Guardar rotaciones activas
        rotationConfig.set("rotations", null);
        int index = 0;
        for (KitRotationData rotationData : activeRotations.values()) {
            String path = "rotations.rotation_" + index;
            rotationConfig.set(path + ".rotated_at", rotationData.getRotatedAt());
            rotationConfig.set(path + ".next_finish_cooldown", rotationData.getNextFinishCooldown());
            rotationConfig.set(path + ".kitId", rotationData.getKitName());
            rotationConfig.set(path + ".active", rotationData.isActive());
            rotationConfig.set(path + ".probability", rotationData.getProbability());
            index++;
        }

        rotationConfig.save();
    }

    public void checkRotations() {
        if (!rotationEnabled) return;

        long currentTime = System.currentTimeMillis();

        // Verificar si es momento de revisar rotaciones
        if (lastRotationCheck != 0 && currentTime - lastRotationCheck < rotationCheckInterval) {
            return;
        }

        lastRotationCheck = currentTime;

        // Remover rotaciones expiradas
        List<String> expiredRotations = new ArrayList<>();
        for (Map.Entry<String, KitRotationData> entry : activeRotations.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredRotations.add(entry.getKey());
            }
        }
        expiredRotations.forEach(activeRotations::remove);

        // Agregar nuevas rotaciones si hay espacio
        int availableSlots = maxActiveRotations - (int) activeRotations.values().stream()
                .filter(KitRotationData::isActive)
                .count();

        if (availableSlots > 0) {
            List<Kit> eligibleKits = getEligibleKitsForRotation();
            Collections.shuffle(eligibleKits);

            for (int i = 0; i < Math.min(availableSlots, eligibleKits.size()); i++) {
                Kit kit = eligibleKits.get(i);
                activateRotation(kit);
            }
        }

        save();
    }

    private List<Kit> getEligibleKitsForRotation() {
        List<Kit> eligibleKits = new ArrayList<>();

        for (Kit kit : playerKits.getKitManager().getKits().values()) {
            if (kit == null) continue;

            PropertyTiming timing = kit.getPropertyTiming();
            if (timing == null || !timing.isRotationEnabled()) continue;

            // Verificar si ya está en rotación activa
            if (activeRotations.containsKey(kit.getName()) &&
                activeRotations.get(kit.getName()).isActive()) {
                continue;
            }

            // Verificar probabilidad
            int probability = timing.getRotationProbability();
            if (probability > 0 && probability < 100) {
                Random random = new Random();
                if (random.nextInt(100) >= probability) {
                    continue;
                }
            }

            eligibleKits.add(kit);
        }

        return eligibleKits;
    }

    public boolean activateRotation(Kit kit) {
        if (!rotationEnabled) return false;
        if (activeRotations.size() >= maxActiveRotations) return false;

        PropertyTiming timing = kit.getPropertyTiming();
        if (!timing.isRotationEnabled()) return false;

        long currentTime = System.currentTimeMillis();
        long endTime = timing.getRotationCooldown() > 0
                ? currentTime + timing.getRotationCooldown()
                : -1L;

        KitRotationData rotationData = new KitRotationData(kit.getName(), currentTime, endTime);
        rotationData.setProbability(timing.getRotationProbability());
        activeRotations.put(kit.getName(), rotationData);

        save();
        return true;
    }

    public boolean deactivateRotation(String kitName) {
        KitRotationData rotationData = activeRotations.get(kitName);
        if (rotationData != null) {
            rotationData.setActive(false);
            save();
            return true;
        }
        return false;
    }

    public boolean removeRotation(String kitName) {
        if (activeRotations.remove(kitName) != null) {
            save();
            return true;
        }
        return false;
    }

    public List<KitRotationData> getActiveRotations() {
        return activeRotations.values().stream()
                .filter(KitRotationData::isActive)
                .collect(Collectors.toList());
    }

    public List<Kit> getActiveRotationKits() {
        return getActiveRotations().stream()
                .map(data -> playerKits.getKitManager().getKit(data.getKitName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public KitRotationData getRotationData(String kitName) {
        return activeRotations.get(kitName);
    }

    public boolean isInRotation(String kitName) {
        KitRotationData data = activeRotations.get(kitName);
        return data != null && data.isActive();
    }

    public long getTimeRemaining(String kitName) {
        KitRotationData data = activeRotations.get(kitName);
        if (data == null || !data.isActive()) return 0L;
        if (data.getNextFinishCooldown() == -1) return -1L;

        long remaining = data.getNextFinishCooldown() - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public void forceRotate() {
        activeRotations.clear();
        checkRotations();
    }

}

