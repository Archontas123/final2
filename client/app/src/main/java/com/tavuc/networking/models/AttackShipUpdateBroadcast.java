package com.tavuc.networking.models;
/**
 * Represents the AttackShipUpdateBroadcast networking message.
 */

public class AttackShipUpdateBroadcast extends BaseMessage {
    public String entityId;
    public String parentCruiserId;
    public int x;
    public int y;
    public float velocityX;
    public float velocityY;
    public float orientation;
    public float health;
    public float maxHealth;
    public String aiState;
    public float attackPointX;
    public float attackPointY;
    public String targetPlayerId;

    public AttackShipUpdateBroadcast() {
        super();
    }
}
