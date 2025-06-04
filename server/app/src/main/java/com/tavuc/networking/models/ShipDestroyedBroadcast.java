package com.tavuc.networking.models;


public class ShipDestroyedBroadcast extends BaseMessage {
    public String playerId;
    public float x;
    public float y;

    public ShipDestroyedBroadcast() {
        this.type = "SHIP_DESTROYED_BROADCAST";
    }

    public ShipDestroyedBroadcast(String playerId, float x, float y) {
        this.type = "SHIP_DESTROYED_BROADCAST";
        this.playerId = playerId;
        this.x = x;
        this.y = y;
    }
}
