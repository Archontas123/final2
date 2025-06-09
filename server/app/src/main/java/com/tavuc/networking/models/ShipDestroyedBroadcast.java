package com.tavuc.networking.models;
/**
 * Represents the ShipDestroyedBroadcast networking message.
 */

public class ShipDestroyedBroadcast extends BaseMessage {
    public String playerId;
    public float x;
    public float y;

    /**
     * Constructs a new ShipDestroyedBroadcast.
     */
    public ShipDestroyedBroadcast() {
        this.type = "SHIP_DESTROYED_BROADCAST";
    }

    /**
     * Constructs a new ShipDestroyedBroadcast.
     */
    public ShipDestroyedBroadcast(String playerId, float x, float y) {
        this.type = "SHIP_DESTROYED_BROADCAST";
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }
}
