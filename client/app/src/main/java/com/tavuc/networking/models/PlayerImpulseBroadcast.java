package com.tavuc.networking.models;

public class PlayerImpulseBroadcast extends BaseMessage {
    public String playerId;
    public double dx;
    public double dy;
    public double freeze;

    public PlayerImpulseBroadcast() {
        this.type = "PLAYER_IMPULSE_BROADCAST";
    }

    public PlayerImpulseBroadcast(String playerId, double dx, double dy, double freeze) {
        this();
        this.playerId = playerId;
        this.dx = dx;
        this.dy = dy;
        this.freeze = freeze;
    }
}
