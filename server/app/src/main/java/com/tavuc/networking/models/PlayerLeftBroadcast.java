package com.tavuc.networking.models;
/**
 * Represents the PlayerLeftBroadcast networking message.
 */

public class PlayerLeftBroadcast extends BaseMessage {
    public String playerId;

    /**
     * Constructs a new PlayerLeftBroadcast.
     */
    public PlayerLeftBroadcast(String playerId) {
        this.type = "PLAYER_LEFT_BROADCAST";
        this.playerId = playerId;
    }
}
