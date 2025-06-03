package com.tavuc.networking.models;

public class PlayerMovedBroadcast extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double dx;
    public double dy;
    public double directionAngle;

    // Gson will use this constructor
    public PlayerMovedBroadcast() {
        this.type = "PLAYER_MOVED_BROADCAST";
    }
}
