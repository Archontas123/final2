package com.tavuc.models.space;

import com.tavuc.models.GameObject;

public abstract class BaseShip extends GameObject {

    protected String entityId;
    protected float health;
    protected float maxHealth;
    protected float orientation;
    protected float maxSpeed;
    protected float acceleration; 
    protected float turnRate; 
    protected float fireRate; 
    protected long lastFireTime; 
    protected float projectileDamage;

    protected float velocityX;
    protected float velocityY;

    public BaseShip(String entityId, int x, int y, int width, int height, float maxHealth, float maxSpeed, float acceleration, float turnRate, float fireRate, float projectileDamage) {
        super(x, y, width, height);
        this.entityId = entityId;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.orientation = 0f; 
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.turnRate = turnRate;
        this.fireRate = fireRate;
        this.projectileDamage = projectileDamage;
        this.lastFireTime = 0;
        this.velocityX = 0f;
        this.velocityY = 0f;
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

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getTurnRate() {
        return turnRate;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
        while (this.orientation < 0) this.orientation += 2 * Math.PI;
        while (this.orientation >= 2 * Math.PI) this.orientation -= 2 * Math.PI;
    }
    
    public void setVelocity(float vx, float vy) {
        this.velocityX = vx;
        this.velocityY = vy;
    }

    public void takeDamage(float amount) {
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public boolean canFire() {
        return System.currentTimeMillis() - lastFireTime >= 1000 / fireRate;
    }

    public float getProjectileDamage() {
        return projectileDamage;
    }



}
