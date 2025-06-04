package com.tavuc.models.space;

/**
 * Represents a projectile fired by a ship.
 * Handles movement, damage, and collision detection.
 */
public class Projectile {
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double damage;
    private String ownerId;
    private boolean active;
    private double lifetime;

    /**
     * Constructor for Projectile.
     *
     * @param x The initial x-coordinate
     * @param y The initial y-coordinate
     * @param velocityX The x-component of velocity
     * @param velocityY The y-component of velocity
     * @param damage The damage this projectile deals
     * @param ownerId The ID of the player who fired this projectile
     */
    public Projectile(double x, double y, double velocityX, double velocityY, double damage, String ownerId) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.ownerId = ownerId;
        this.active = true;
        this.lifetime = 0.0;
    }

    /**
     * Updates the projectile's position based on its velocity.
     *
     * @param delta Time passed since last update in seconds
     */
    public void tick(double delta) {
        this.x += velocityX * delta;
        this.y += velocityY * delta;
        this.lifetime += delta;
    }

    /**
     * Gets the current x-coordinate.
     *
     * @return The x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the current y-coordinate.
     *
     * @return The y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the damage this projectile deals.
     *
     * @return The damage amount
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Gets the ID of the player who fired this projectile.
     *
     * @return The owner ID
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Checks if this projectile is active.
     * Inactive projectiles will be removed from the game.
     *
     * @return True if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this projectile is active.
     *
     * @param active True to set active, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Gets the x-component of velocity.
     *
     * @return The x-velocity
     */
    public double getVelocityX() {
        return velocityX;
    }
    
    /**
     * Gets the y-component of velocity.
     *
     * @return The y-velocity
     */
    public double getVelocityY() {
        return velocityY;
    }
    
    /**
     * Gets the lifetime of this projectile in seconds.
     *
     * @return The lifetime in seconds
     */
    public double getLifetime() {
        return lifetime;
    }
    
    /**
     * Sets the x-coordinate.
     *
     * @param x The new x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /**
     * Sets the y-coordinate.
     *
     * @param y The new y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Sets the x-component of velocity.
     *
     * @param velocityX The new x-velocity
     */
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }
    
    /**
     * Sets the y-component of velocity.
     *
     * @param velocityY The new y-velocity
     */
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }
}