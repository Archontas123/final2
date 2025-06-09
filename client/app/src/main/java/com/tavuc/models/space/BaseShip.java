package com.tavuc.models.space;

import com.tavuc.models.GameObject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * An abstract base class for all ship-like objects in the space environment.
 */
public abstract class BaseShip extends GameObject {

    /**
     * The unique identifier for this ship entity.
     */
    protected String entityId;
    /**
     * The current health of the ship.
     */
    protected float health;
    /**
     * The maximum possible health of the ship.
     */
    protected float maxHealth;
    /**
     * The current orientation (rotation) of the ship in radians.
     */
    protected float orientation; 
    /**
     * The target x-coordinate received from the server, used for interpolation.
     */
    protected double targetX;
    /**
     * The target y-coordinate received from the server, used for interpolation.
     */
    protected double targetY;
    /**
     * The target orientation received from the server, used for interpolation.
     */
    protected float targetOrientation;
    /**
     * The timestamp of the last time the network state was updated.
     */
    protected long lastUpdateTime;

    /**
     * The visual representation of the ship. Marked as transient to prevent serialization.
     */
    protected transient BufferedImage sprite; 

    /**
     * Constructs a new BaseShip with initial properties.
     * @param entityId The unique identifier for the ship.
     * @param x The initial x-coordinate.
     * @param y The initial y-coordinate.
     * @param width The width of the ship.
     * @param height The height of the ship.
     * @param initialMaxHealth The maximum health value for the ship.
     */
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

    /**
     * Updates the target state of the ship with new data received from the network.
     * @param newX The new target x-coordinate.
     * @param newY The new target y-coordinate.
     * @param newOrientation The new target orientation in radians.
     * @param newHealth The updated current health.
     * @param newMaxHealth The updated maximum health.
     */
    public void updateNetworkState(double newX, double newY, float newOrientation, float newHealth, float newMaxHealth) {
        this.targetX = newX;
        this.targetY = newY;
        this.targetOrientation = newOrientation; 
        this.health = newHealth;
        this.maxHealth = newMaxHealth; 
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Smoothly interpolates the ship's current rendered state towards its target state. 
     * @param alpha The interpolation factor, typically a value between 0 and 1,
     *              representing how far to move towards the target in this frame.
     */
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


    /**
     * Gets the unique entity ID of the ship.
     * @return The entity ID string.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Gets the current health of the ship.
     * @return The current health value.
     */
    public float getHealth() {
        return health;
    }

    /**
     * Gets the maximum health of the ship.
     * @return The maximum health value.
     */
    public float getMaxHealth() {
        return maxHealth;
    }

    /**
     * Gets the current orientation of the ship.
     * @return The orientation in radians.
     */
    public float getOrientation() {
        return orientation;
    }

    /**
     * Sets the current health of the ship, clamping the value between 0 and maxHealth.
     * @param health The new health value.
     */
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(health, this.maxHealth));
    }

    /**
     * Sets the orientation of the ship.
     * @param orientation The new orientation in radians.
     */
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }
    
    /**
     * Sets the visual sprite for the ship.
     * @param sprite The {@link BufferedImage} to use for rendering.
     */
    public void setSprite(BufferedImage sprite) {
        this.sprite = sprite;
    }

    /**
     * Gets the visual sprite for the ship.
     * @return The {@link BufferedImage} used for rendering.
     */
    public BufferedImage getSprite() {
        return sprite;
    }
}