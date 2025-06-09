package com.tavuc.networking.models;
/**
 * Represents the PlayerMovedBroadcast networking message.
 */

public class PlayerMovedBroadcast extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double dx;
    public double dy;
    public double directionAngle;

    /**
     * Constructs a new PlayerMovedBroadcast.
     */
    public PlayerMovedBroadcast(String playerId, double x, double y, double dx, double dy, double directionAngle) {
        this.type = "PLAYER_MOVED_BROADCAST";
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.directionAngle = directionAngle;
    }
}
