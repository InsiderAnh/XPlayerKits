package io.github.InsiderAnh.xPlayerKits.managers;

import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.config.InsiderConfig;
import io.github.InsiderAnh.xPlayerKits.data.KitVotingData;
import io.github.InsiderAnh.xPlayerKits.kits.Kit;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class VotingManager {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private final Map<String, KitVotingData> activeVotings = new ConcurrentHashMap<>();
    private InsiderConfig votingConfig;
    private boolean votingEnabled;
    private long votingDuration;
    private boolean autoActivation;
    private long autoActivationInterval;
    private int maxKitsPerVoting;
    private long lastAutoActivation;

    public VotingManager() {
        this.votingConfig = new InsiderConfig(playerKits, "voting", true, false);
        load();
    }

    public void load() {
        this.votingEnabled = votingConfig.getBooleanOrDefault("enabled", true);
        this.votingDuration = votingConfig.getLongOrDefault("duration", 86400000L); // 24 horas por defecto
        this.autoActivation = votingConfig.getBooleanOrDefault("autoActivation", false);
        this.autoActivationInterval = votingConfig.getLongOrDefault("autoActivationInterval", 604800000L); // 7 días
        this.maxKitsPerVoting = votingConfig.getIntOrDefault("maxKitsPerVoting", 5);
        this.lastAutoActivation = votingConfig.getLongOrDefault("lastAutoActivation", 0L);

        // Cargar votaciones activas
        if (votingConfig.isSet("activeVotings")) {
            for (String key : votingConfig.getConfig().getConfigurationSection("activeVotings").getKeys(false)) {
                String kitName = votingConfig.getString("activeVotings." + key + ".kitName");
                int totalVotes = votingConfig.getIntOrDefault("activeVotings." + key + ".totalVotes", 0);
                long startTime = votingConfig.getLongOrDefault("activeVotings." + key + ".votingStartTime", 0L);
                long endTime = votingConfig.getLongOrDefault("activeVotings." + key + ".votingEndTime", 0L);
                boolean active = votingConfig.getBooleanOrDefault("activeVotings." + key + ".active", true);

                KitVotingData votingData = new KitVotingData(kitName);
                votingData.setTotalVotes(totalVotes);
                votingData.setVotingStartTime(startTime);
                votingData.setVotingEndTime(endTime);
                votingData.setActive(active);

                // Cargar votos de jugadores
                if (votingConfig.isSet("activeVotings." + key + ".playerVotes")) {
                    Map<String, Object> votes = votingConfig.getConfig().getConfigurationSection("activeVotings." + key + ".playerVotes").getValues(false);
                    Map<String, Integer> playerVotes = new HashMap<>();
                    votes.forEach((uuid, voteCount) -> playerVotes.put(uuid, (Integer) voteCount));
                    votingData.setPlayerVotes(playerVotes);
                }

                activeVotings.put(kitName, votingData);
            }
        }

        checkAutoActivation();
    }

    public void save() {
        votingConfig.set("enabled", votingEnabled);
        votingConfig.set("duration", votingDuration);
        votingConfig.set("autoActivation", autoActivation);
        votingConfig.set("autoActivationInterval", autoActivationInterval);
        votingConfig.set("maxKitsPerVoting", maxKitsPerVoting);
        votingConfig.set("lastAutoActivation", lastAutoActivation);

        // Guardar votaciones activas
        votingConfig.set("activeVotings", null);
        int index = 0;
        for (KitVotingData votingData : activeVotings.values()) {
            String path = "activeVotings." + index;
            votingConfig.set(path + ".kitName", votingData.getKitName());
            votingConfig.set(path + ".totalVotes", votingData.getTotalVotes());
            votingConfig.set(path + ".votingStartTime", votingData.getVotingStartTime());
            votingConfig.set(path + ".votingEndTime", votingData.getVotingEndTime());
            votingConfig.set(path + ".active", votingData.isActive());

            // Guardar votos de jugadores
            if (!votingData.getPlayerVotes().isEmpty()) {
                votingData.getPlayerVotes().forEach((uuid, voteCount) -> {
                    votingConfig.set(path + ".playerVotes." + uuid, voteCount);
                });
            }
            index++;
        }

        votingConfig.save();
    }

    public boolean startVoting(Kit kit) {
        if (!votingEnabled) return false;
        if (activeVotings.containsKey(kit.getName())) return false;

        KitVotingData votingData = new KitVotingData(kit.getName());
        votingData.setVotingEndTime(System.currentTimeMillis() + votingDuration);
        activeVotings.put(kit.getName(), votingData);
        save();
        return true;
    }

    public boolean startVotingMultiple(List<Kit> kits) {
        if (!votingEnabled) return false;
        if (kits.isEmpty()) return false;

        boolean anyStarted = false;
        for (Kit kit : kits) {
            if (activeVotings.size() >= maxKitsPerVoting) break;
            if (!activeVotings.containsKey(kit.getName())) {
                KitVotingData votingData = new KitVotingData(kit.getName());
                votingData.setVotingEndTime(System.currentTimeMillis() + votingDuration);
                activeVotings.put(kit.getName(), votingData);
                anyStarted = true;
            }
        }
        if (anyStarted) save();
        return anyStarted;
    }

    public boolean vote(Player player, String kitName) {
        KitVotingData votingData = activeVotings.get(kitName);
        if (votingData == null || !votingData.isActive()) return false;
        if (votingData.getVotingEndTime() != 0 && System.currentTimeMillis() > votingData.getVotingEndTime()) {
            endVoting(kitName);
            return false;
        }

        if (votingData.hasVoted(player.getUniqueId().toString())) {
            return false; // Ya votó
        }

        votingData.addVote(player.getUniqueId().toString());
        save();
        return true;
    }

    public void endVoting(String kitName) {
        KitVotingData votingData = activeVotings.get(kitName);
        if (votingData != null) {
            votingData.endVoting();
            save();
        }
    }

    public void endAllVotings() {
        activeVotings.values().forEach(KitVotingData::endVoting);
        save();
    }

    public List<KitVotingData> getTopVotedKits(int limit) {
        return activeVotings.values().stream()
                .sorted(Comparator.comparingInt(KitVotingData::getTotalVotes).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<KitVotingData> getActiveVotings() {
        return activeVotings.values().stream()
                .filter(KitVotingData::isActive)
                .collect(Collectors.toList());
    }

    public KitVotingData getVotingData(String kitName) {
        return activeVotings.get(kitName);
    }

    public void checkAutoActivation() {
        if (!autoActivation) return;

        long currentTime = System.currentTimeMillis();
        if (lastAutoActivation == 0 || currentTime - lastAutoActivation >= autoActivationInterval) {
            // Seleccionar kits aleatorios para votación
            List<Kit> allKits = new ArrayList<>(playerKits.getKitManager().getKits().values());
            allKits.removeIf(Objects::isNull);
            Collections.shuffle(allKits);

            List<Kit> selectedKits = allKits.stream()
                    .limit(maxKitsPerVoting)
                    .collect(Collectors.toList());

            if (startVotingMultiple(selectedKits)) {
                lastAutoActivation = currentTime;
                save();
            }
        }
    }

    public boolean removeVoting(String kitName) {
        if (activeVotings.remove(kitName) != null) {
            save();
            return true;
        }
        return false;
    }

}

