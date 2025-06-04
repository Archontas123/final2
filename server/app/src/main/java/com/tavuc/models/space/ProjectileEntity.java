package com.tavuc.models.space;

/**
 * Represents a projectile entity on the server side.
 */
public class ProjectileEntity {
    private String id;
    private float x;
    private float y;
    private int width;
    private int height;
    private float orientation;
    private float velocityX;
    private float velocityY;
    private float damage;
    private String ownerId;
    private float lifetime;
    
    /**
     * Constructor for ProjectileEntity.
     * 
     * @param id Unique identifier for the projectile
     * @param x Initial X position
     * @param y Initial Y position
     * @param width Width of the projectile
     * @param height Height of the projectile
     * @param orientation Direction angle in radians
     * @param velocityX X velocity component
     * @param velocityY Y velocity component
     * @param damage Damage amount
     * @param ownerId ID of the entity that fired this projectile
     */
    public ProjectileEntity(String id, float x, float y, int width, int height, 
                            float orientation, float velocityX, float velocityY, 
                            float damage, String ownerId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.ownerId = ownerId;
        this.lifetime = 0;
    }
    
    /**
     * Updates the projectile's position based on its velocity.
     * 
     * @param deltaTime Time passed since last update in seconds
     */
    public void update(float deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        lifetime += deltaTime;
    }
    
    /**
     * Gets the projectile's unique identifier.
     * 
     * @return The projectile ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the projectile's current X position.
     * 
     * @return X position
     */
    public float getX() {
        return x;
    }
    
    /**
     * Gets the projectile's current Y position.
     * 
     * @return Y position
     */
    public float getY() {
        return y;
    }
    
    /**
     * Gets the projectile's width.
     * 
     * @return Width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the projectile's height.
     * 
     * @return Height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gets the projectile's orientation angle.
     * 
     * @return Orientation in radians
     */
    public float getOrientation() {
        return orientation;
    }
    
    /**
     * Gets the projectile's X velocity component.
     * 
     * @return X velocity
     */
    public float getVelocityX() {
        return velocityX;
    }
    
    /**
     * Gets the projectile's Y velocity component.
     * 
     * @return Y velocity
     */
    public float getVelocityY() {
        return velocityY;
    }
    
    /**
     * Gets the damage amount this projectile deals.
     * 
     * @return Damage amount
     */
    public float getDamage() {
        return damage;
    }
    
    /**
     * Gets the ID of the entity that fired this projectile.
     * 
     * @return Owner entity ID
     */
    public String getOwnerId() {
        return ownerId;
    }
    
    /**
     * Gets the lifetime of this projectile in seconds.
     * 
     * @return Lifetime in seconds
     */
    public float getLifetime() {
        return lifetime;
    }
}