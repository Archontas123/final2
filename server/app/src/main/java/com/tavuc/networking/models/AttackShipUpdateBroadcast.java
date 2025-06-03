package com.tavuc.networking.models;

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

    public AttackShipUpdateBroadcast(String entityId, String parentCruiserId, int x, int y,
                                     float velocityX, float velocityY, float orientation,
                                     float health, float maxHealth, String aiState,
                                     float attackPointX, float attackPointY) {
        super(); 
        this.type = "ATTACK_SHIP_UPDATE_BROADCAST"; 
        this.entityId = entityId;
        this.parentCruiserId = parentCruiserId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.orientation = orientation;
        this.health = health;
        this.maxHealth = maxHealth;
        this.aiState = aiState;
        this.attackPointX = attackPointX;
        this.attackPointY = attackPointY;
    }
}
