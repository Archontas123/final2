package com.tavuc.networking.models;

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

    public CruiserUpdateBroadcast(String entityId, int x, int y, float velocityX, float velocityY,
                                  float orientation, float health, float maxHealth,
                                  float shieldHealth, float maxShieldHealth, String aiState) {
        super(); 
        this.type = "CRUISER_UPDATE_BROADCAST"; 
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.orientation = orientation;
        this.health = health;
        this.maxHealth = maxHealth;
        this.shieldHealth = shieldHealth;
        this.maxShieldHealth = maxShieldHealth;
        this.aiState = aiState;
    }
}
