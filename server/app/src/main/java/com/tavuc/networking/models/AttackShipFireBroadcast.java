package com.tavuc.networking.models;
/**
 * Represents the AttackShipFireBroadcast networking message.
 */

public class AttackShipFireBroadcast extends BaseMessage {
    public String attackerId;
    public String targetPlayerId;
    public int fromX;
    public int fromY;
    public int toX;
    public int toY;

    /**
     * Constructs a new AttackShipFireBroadcast.
     */
    public AttackShipFireBroadcast(String attackerId, String targetPlayerId, int fromX, int fromY, int toX, int toY) {
        super();
        this.type = "ATTACK_SHIP_FIRE_BROADCAST";
        this.attackerId = attackerId;
        this.targetPlayerId = targetPlayerId;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }
}
