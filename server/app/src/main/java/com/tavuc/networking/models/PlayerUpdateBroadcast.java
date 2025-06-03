package com.tavuc.networking.models;

public class PlayerUpdateBroadcast extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double dx;
    public double dy;
    public double directionAngle;

    public PlayerUpdateBroadcast() {
    }

    public PlayerUpdateBroadcast(String playerId, double x, double y, double dx, double dy, double directionAngle) {
        this.type = "PLAYER_UPDATE_BROADCAST";
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.directionAngle = directionAngle;
    }
}
