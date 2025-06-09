package com.tavuc.networking.models;
/**
 * Represents the PlayerInitialData networking message.
 */

public class PlayerInitialData {
    public String playerId;
    public String username;
    public double x;
    public double y;
    public double dx;
    public double dy;
    public double directionAngle;

    public PlayerInitialData() {
    }

    public PlayerInitialData(String playerId, String username, double x, double y, double dx, double dy, double directionAngle) {
        this.playerId = playerId;
        this.username = username;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.directionAngle = directionAngle;
    }
}
