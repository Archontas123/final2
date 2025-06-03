package com.tavuc.networking.models;

public class PlayerLeftBroadcast extends BaseMessage {
    public String playerId;

    // Gson will use this constructor
    public PlayerLeftBroadcast() {
        this.type = "PLAYER_LEFT_BROADCAST";
    }
}
