package com.tavuc.networking.models;
/**
 * Represents the PlayerLeftBroadcast networking message.
 */

public class PlayerLeftBroadcast extends BaseMessage {
    public String playerId;


    public PlayerLeftBroadcast() {
        this.type = "PLAYER_LEFT_BROADCAST";
    }
}
