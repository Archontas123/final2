package com.tavuc.networking.models;
/**
 * Represents the CruiserUpdateBroadcast networking message.
 */

public class CruiserUpdateBroadcast extends BaseMessage {
    public String entityId;
    public int x;
    public int y;
    public float velocityX;
    public float velocityY;
    public float orientation;
    public float health;
    public float maxHealth;
    public float shieldHealth;
    public float maxShieldHealth;
    public String aiState;

    public CruiserUpdateBroadcast() {
        super();
    }
}
