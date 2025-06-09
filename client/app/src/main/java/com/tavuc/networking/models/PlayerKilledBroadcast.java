package com.tavuc.networking.models;
/**
 * Represents the PlayerKilledBroadcast networking message.
 */

public class PlayerKilledBroadcast extends BaseMessage {
    public String playerId;
    public String killerId;

    /**
     * Constructs a new PlayerKilledBroadcast.
     */
    public PlayerKilledBroadcast() {
        this.type = "PLAYER_KILLED_BROADCAST";
    }

    /**
     * Constructs a new PlayerKilledBroadcast.
     */
    public PlayerKilledBroadcast(String playerId, String killerId) {
        this();
        this.playerId = playerId;
        this.killerId = killerId;
    }
}
