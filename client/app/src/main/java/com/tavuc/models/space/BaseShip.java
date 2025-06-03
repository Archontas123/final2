package com.tavuc.models.space;

import com.tavuc.models.GameObject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public abstract class BaseShip extends GameObject {

    protected String entityId;
    protected float health;
    protected float maxHealth;
    protected float orientation; 
    protected double targetX;
    protected double targetY;
    protected float targetOrientation;
    protected long lastUpdateTime;

    protected transient BufferedImage sprite; 

    public BaseShip(String entityId, double x, double y, int width, int height, float initialMaxHealth) {
        super(x, y, width, height);
        this.entityId = entityId;
        this.maxHealth = initialMaxHealth;
        this.health = initialMaxHealth;
        this.orientation = 0f;

        this.targetX = x;
        this.targetY = y;
        this.targetOrientation = 0f;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updateNetworkState(double newX, double newY, float newOrientation, float newHealth, float newMaxHealth) {
        this.targetX = newX;
        this.targetY = newY;
        this.targetOrientation = newOrientation; 
        this.health = newHealth;
        this.maxHealth = newMaxHealth; 
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void interpolate(float alpha) { 
        this.x = this.x + (this.targetX - this.x) * alpha;
        this.y = this.y + (this.targetY - this.y) * alpha;
        
        double angleDiff = this.targetOrientation - this.orientation;
        while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
        while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;
        
        this.orientation += angleDiff * alpha;
        while (this.orientation < 0) this.orientation += 2 * Math.PI;
        while (this.orientation >= 2 * Math.PI) this.orientation -= 2 * Math.PI;

        updateHitbox(); 
    }


    public String getEntityId() {
        return entityId;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(health, this.maxHealth));
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }
    
    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    public BufferedImage getSprite() {
        return sprite;
    }
}
