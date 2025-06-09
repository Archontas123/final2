package com.tavuc.networking.models;

public class PlayerShootRequest extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double directionAngle;

    public PlayerShootRequest() {
        this.type = "PLAYER_SHOOT_REQUEST";
    }

    public PlayerShootRequest(String playerId, double x, double y, double directionAngle) {
        this();
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.directionAngle = directionAngle;
    }
}
