package com.tavuc.networking.models;
/**
 * Represents the ShipUpdateBroadcast networking message.
 */

public class ShipUpdateBroadcast extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double angle;
    public double dx;
    public double dy;
    public boolean thrusting;

    /**
     * Constructs a new ShipUpdateBroadcast.
     */
    public ShipUpdateBroadcast() {
    }

    /**
     * Constructs a new ShipUpdateBroadcast.
     */
    public ShipUpdateBroadcast(String playerId, double x, double y, double angle, double dx, double dy, boolean thrusting) {
        this.type = "SHIP_UPDATE_BROADCAST";
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.dx = dx;
        this.dy = dy;
        this.thrusting = thrusting;
    }
}
