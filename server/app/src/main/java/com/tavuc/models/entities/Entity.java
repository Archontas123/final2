package com.tavuc.models.entities;

import java.awt.Rectangle;

import com.tavuc.models.GameObject;


public class Entity extends GameObject {
    
    private int id;
    private String name;
    private Rectangle hurtbox;
    private double dx;
    private double dy;
    private double health;
    private double acceleration;
    private long frozenUntil = 0;
    private double pushVelX = 0;
    private double pushVelY = 0;
    private long pushUntil = 0;

    /**
     * Constructor for Entity
     * @param id The ID of the entity
     * @param name The name of the entity
     * @param x The x position of the entity
     * @param y The y position of the entity
     * @param health The health of the entity
     * @param speed The speed of the entity
     */
    public Entity(int id, String name, int x, int y, double health,int width, int height) {
        super(x, y,width, height); 
        this.id = id;
        this.name = name;
        this.health = health;
        this.acceleration = 0.0;
        this.dx = 0.0;
        this.dy = 0.0;

        int hurtboxWidth = (int) (width * 0.8); 
        int hurtboxHeight = (int) (height * 0.8);
        int hurtboxX = x + (width - hurtboxWidth) / 2;
        int hurtboxY = y + (height - hurtboxHeight) / 2;
        this.hurtbox = new Rectangle(hurtboxX, hurtboxY, hurtboxWidth, hurtboxHeight);
    }

    /**
     * Gets the players ID
     * @return players ID
     */
    public int getId() {
        return id;
    }
    /**
     * Gets the dx (change in x) of the player.
     * @return the dx
     */
    public double getDx() {
        return dx;
    }

    /**
     * Sets the dx (change in x) of the player.
     * @param dx the new dx
     */
    public void setDx(double dx) {
        this.dx = dx;
    }

    /**
     * Gets the dy (change in y) of the player.
     * @return the dy
     */
    public double getDy() {
        return dy;
    }

    /**
     * Sets the dy (change in y) of the player.
     * @param dy the new dy
     */
    public void setDy(double dy) {
        this.dy = dy;
    }
    
    /**
     * Gets the health of the player.
     * @return the health
     */
    public double getHealth() {
        return health;
    }

    /**
     * Sets the health of the player.
     * @param health the new health
     */
    public void setHealth(double health) {
        this.health = health;
    }

    /**
     * Reduces the entity's health by the specified amount.
     * Health will not drop below zero.
     * @param amount The damage amount
     */
    public void takeDamage(double amount) {
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
        // Taking damage breaks any freeze effect
        this.frozenUntil = 0;
    }

    /**
     * Gets the acceleration of the player.
     * @return the acceleration
     */
    public double getAcceleration() {
        return acceleration;
    }

    /**
     * Sets the acceleration of the player.
     * @param acceleration the new acceleration
     */
    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Freezes the entity for the specified duration in milliseconds.
     */
    public void freeze(long durationMs) {
        this.frozenUntil = System.currentTimeMillis() + durationMs;
    }

    /**
     * Removes any active freeze effect.
     */
    public void unfreeze() {
        this.frozenUntil = 0;
    }

    /**
     * Returns whether the entity is currently frozen.
     */
    public boolean isFrozen() {
        return System.currentTimeMillis() < this.frozenUntil;
    }

    /**
     * Applies a push or pull velocity for the specified duration.
     */
    public void applyPush(double velX, double velY, long durationMs) {
        this.pushVelX = velX;
        this.pushVelY = velY;
        this.pushUntil = System.currentTimeMillis() + durationMs;
    }

    /**
     * Updates the player state based on dx and dy.
     * This method should be called by the server to reflect changes from client or server-side logic.
     */
    @Override
    public void update() {
        if (!isFrozen()) {
            int newX = getX();
            int newY = getY();

            if (System.currentTimeMillis() < this.pushUntil) {
                newX += (int) this.pushVelX;
                newY += (int) this.pushVelY;
            } else if (this.pushVelX != 0 || this.pushVelY != 0) {
                this.pushVelX = 0;
                this.pushVelY = 0;
            }

            newX += (int) this.dx;
            newY += (int) this.dy;

            setPosition(newX, newY);
        }
        updateHurtbox();
    }

    /**
     * Repositions the hurtbox to stay centered on the entity.
     */
    private void updateHurtbox() {
        this.hurtbox.setLocation(
            getX() + (getWidth() - this.hurtbox.width) / 2,
            getY() + (getHeight() - this.hurtbox.height) / 2
        );
    }

    /**
     * Gets the hurtbox of the entity.
     * @return the hurtbox
     */
    public Rectangle getHurtbox() {
        updateHurtbox();
        return hurtbox.getBounds();
    }

    /**
     * Checks if this entity has collided with another game object and handles the response.
     * @param other The other game object to check for collision.
     */
    public void collided(GameObject other) {
        // Collision detection removed
    }
    
}
