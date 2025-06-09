package com.tavuc.networking.models;
/**
 * Represents the ShipLeftBroadcast networking message.
 */

public class ShipLeftBroadcast extends BaseMessage {
    public String playerId;

    public ShipLeftBroadcast() {
    }

    public ShipLeftBroadcast(String playerId) {
        this.type = "SHIP_LEFT_BROADCAST";
        this.playerId = playerId;
    }
}
