package io.github.InsiderAnh.xPlayerKits.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class KitVotingData {

    private String kitName;
    private Map<String, Integer> playerVotes; // UUID -> vote count
    private int totalVotes;
    private long votingStartTime;
    private long votingEndTime;
    private boolean active;

    public KitVotingData(String kitName) {
        this.kitName = kitName;
        this.playerVotes = new HashMap<>();
        this.totalVotes = 0;
        this.votingStartTime = System.currentTimeMillis();
        this.votingEndTime = 0;
        this.active = true;
    }

    public void addVote(String playerUuid) {
        playerVotes.put(playerUuid, playerVotes.getOrDefault(playerUuid, 0) + 1);
        totalVotes++;
    }

    public boolean hasVoted(String playerUuid) {
        return playerVotes.containsKey(playerUuid);
    }

    public void endVoting() {
        this.active = false;
        this.votingEndTime = System.currentTimeMillis();
    }

}

