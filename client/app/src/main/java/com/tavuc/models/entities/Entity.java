package com.tavuc.models.entities;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.tavuc.Client;
import com.tavuc.models.GameObject;

public abstract class Entity extends GameObject {
    
    protected double dx, dy; 
    protected double velocity;
    protected int health;
    protected int maxHealth;
    protected Rectangle hurtbox;

    /**
     * Constructor for Entity
     * @param x The x position of the entity
     * @param y The y position of the entity
     * @param width The width of the entity
     * @param height The height of the entity
     * @param velocity The velocity of the entity
     * @param maxHealth The maximum health of the entity
     */
    public Entity(double x, double y, int width, int height, double velocity, int maxHealth) {
        super(x, y, width, height);
        this.velocity = velocity;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.dx = 0;
        this.dy = 0;
        int hurtboxWidth = (int) (width * 0.8);
        int hurtboxHeight = (int) (height * 0.8);
        this.hurtbox = new Rectangle((int)x + (width - hurtboxWidth) / 2, (int)y + (height - hurtboxHeight) / 2, hurtboxWidth, hurtboxHeight);
    }

    /**
     * Gets the dx of the entity.
     * @return the dx value
     */
    public double getDx() {
        return dx;
    }

    /**
     * Sets the dx of the entity.
     * @param dx the new dx value
     */
    public void setDx(double dx) {
        this.dx = dx;
    }

    /**
     * Gets the dy of the entity.
     * @return the dy value
     */
    public double getDy() {
        return dy;
    }

    /**
     * Sets the dy of the entity.
     * @param dy the new dy value
     */
    public void setDy(double dy) {
        this.dy = dy;
    }

    /**
     * Gets the velocity of the entity.
     * @return the velocity value
     */
    public double getVelocity() {
        return velocity;
    }

    /**
     * Sets the velocity of the entity.
     * @param velocity the new velocity value
     */
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    /**
     * Gets the health of the entity.
     * @return the current health
     */
    public int getHealth() {
        return health;
    }

    /**
     * Sets the health of the entity, ensuring it does not exceed maxHealth or drop below 0.
     * @param health the new health value
     */
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    /**
     * Gets the maximum health of the entity.
     * @return the maximum health
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Sets the maximum health of the entity.
     * If the current health exceeds the new maxHealth, it is adjusted to match.
     * @param maxHealth the new maximum health value
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
    }

    /**
     * Checks if the entity is alive.
     * @return true if the entity is alive, false otherwise
     */
    public boolean isAlive() {
        return health > 0;
    }

    /**
     * Applies damage to the entity, reducing its health.
     * @param amount the amount of damage to apply
     */
    public void takeDamage(int amount) {
        setHealth(health - amount);
    }

    /**
     * Heals the entity, increasing its health.
     * @param amount the amount of health to restore
     */
    public void heal(int amount) {
        setHealth(health + amount);
    }

    /**
     * Moves the entity based on its dx and dy values.
     * Updates the hitbox & hurtbox after moving.
     */
    public void move() {
        double newX = x + dx;
        double newY = y + dy;

        if (Client.worldManager != null) {
            com.tavuc.models.planets.Tile tx = Client.worldManager.getTileAt((int)newX, (int)y);
            if (tx != null && tx.isSolid()) {
                newX = x; // block horizontal movement
            }
            com.tavuc.models.planets.Tile ty = Client.worldManager.getTileAt((int)x, (int)newY);
            if (ty != null && ty.isSolid()) {
                newY = y; // block vertical movement
            }
        }

        x = newX;
        y = newY;
        updateHitbox();

        
    }

    /**
     * Gets the hurtbox of the entity.
     * @return the hurtbox
     */
    public Rectangle getHurtbox() {
        // Ensure the hurtbox location matches the current entity position
        updateHitbox();
        return new Rectangle(hurtbox);
    }

    /**
     * Updates the entity's state.
     */
    public abstract void update();

    /**
     * Draws the entity on the provided Graphics2D context.
     * @param g2d the Graphics2D context to draw on
     * @param offsetX the x offset for drawing
     * @param offsetY the y offset for drawing
     */
    @Override
    public abstract void draw(Graphics2D g2d, double offsetX, double offsetY);

    @Override
    protected void updateHitbox() {
        super.updateHitbox();
        this.hurtbox.setLocation((int)this.x + (this.width - this.hurtbox.width) / 2, (int)this.y + (this.height - this.hurtbox.height) / 2);
        
    }
}
