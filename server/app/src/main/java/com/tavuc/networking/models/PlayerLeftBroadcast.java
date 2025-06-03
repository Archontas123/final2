package com.tavuc.networking.models;

public class PlayerLeftBroadcast extends BaseMessage {
    public String playerId;

    public PlayerLeftBroadcast(String playerId) {
        this.type = "PLAYER_LEFT_BROADCAST";
        this.playerId = playerId;
    }
}
