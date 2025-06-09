package com.tavuc.networking.models;
/**
 * Represents the PlayerKilledBroadcast networking message.
 */

public class PlayerKilledBroadcast extends BaseMessage {
    public String playerId;
    public String killerId;

    public PlayerKilledBroadcast() {
        this.type = "PLAYER_KILLED_BROADCAST";
    }

    public PlayerKilledBroadcast(String playerId, String killerId) {
        this();
        this.playerId = playerId;
        this.killerId = killerId;
    }
}
