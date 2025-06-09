package com.tavuc.networking.models;
/**
 * Represents the PlayerShootRequest networking message.
 */

public class PlayerShootRequest extends BaseMessage {
    public String playerId;
    public double x;
    public double y;
    public double directionAngle;

    /**
     * Constructs a new PlayerShootRequest.
     */
    public PlayerShootRequest() {
        this.type = "PLAYER_SHOOT_REQUEST";
    }

    /**
     * Constructs a new PlayerShootRequest.
     */
    public PlayerShootRequest(String playerId, double x, double y, double directionAngle) {
        this();
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.directionAngle = directionAngle;
    }
}
