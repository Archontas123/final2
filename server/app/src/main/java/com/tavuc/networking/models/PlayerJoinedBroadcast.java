package com.tavuc.networking.models;

public class PlayerJoinedBroadcast extends BaseMessage {
    public String playerId;
    public String username;
    public double x;
    public double y;
    public double dx;
    public double dy;
    public double directionAngle;

    public PlayerJoinedBroadcast(String playerId, String username, double x, double y, double dx, double dy, double directionAngle) {
        this.type = "PLAYER_JOINED_BROADCAST";
        this.playerId = playerId;
        this.username = username;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.directionAngle = directionAngle;
    }
}
