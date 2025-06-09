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

    public PlayerMovedBroadcast() {
        this.type = "PLAYER_MOVED_BROADCAST";
    }
}
