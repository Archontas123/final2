package com.tavuc.networking.models;
/**
 * Represents the ShipUpdateRequest networking message.
 */

public class ShipUpdateRequest extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double angle;
    public double dx;
    public double dy;
    public boolean thrusting;

    public ShipUpdateRequest(String playerId, double x, double y, double angle, double dx, double dy, boolean thrusting) {
        this.type = "SHIP_UPDATE_REQUEST";
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.dx = dx;
        this.dy = dy;
        this.thrusting = thrusting;
    }
}
