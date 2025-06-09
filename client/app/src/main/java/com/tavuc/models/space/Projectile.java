package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.geom.Path2D;

/**
 * Represents a projectile fired by a ship in the game. 
 */
public class Projectile {
    /** The unique identifier for this projectile. */
    private String id;
    /** The current x-coordinate of the projectile in the world. */
    private double x;
    /** The current y-coordinate of the projectile in the world. */
    private double y;
    /** The velocity of the projectile along the x-axis. */
    private double velocityX;
    /** The velocity of the projectile along the y-axis. */
    private double velocityY;
    /** The amount of damage this projectile inflicts upon collision. */
    private double damage;
    /** The ID of the entity that fired this projectile. */
    private String ownerId;
    /** A flag indicating if the projectile is active. Inactive projectiles are typically removed. */
    private boolean active;
    /** The total time, in seconds, that this projectile has been active. */
    private double lifetime;
    
    /** A flag to enable or disable debug rendering for projectiles. */
    private static final boolean DEBUG_MODE = true;
    /** The color used for rendering the debug trail. */
    private static final Color DEBUG_TRAIL_COLOR = new Color(255, 0, 0, 100);
    /** The maximum number of points to store for the debug trail. */
    private static final int MAX_TRAIL_POINTS = 20;
    /** An array storing the historical x-coordinates for the debug trail. */
    private double[] trailX = new double[MAX_TRAIL_POINTS];
    /** An array storing the historical y-coordinates for the debug trail. */
    private double[] trailY = new double[MAX_TRAIL_POINTS];
    /** The current index in the circular buffer for the trail points. */
    private int trailIndex = 0;
    /** A flag indicating if the trail buffer has been fully populated at least once. */
    private boolean trailInitialized = false;

    /**
     * Constructs a new Projectile instance.
     * @param id The unique identifier for this projectile.
     * @param x The initial x-coordinate in the world.
     * @param y The initial y-coordinate in the world.
     * @param velocityX The initial velocity on the x-axis.
     * @param velocityY The initial velocity on the y-axis.
     * @param damage The amount of damage this projectile deals on impact.
     * @param ownerId The unique ID of the entity that fired this projectile.
     */
    public Projectile(String id, double x, double y, double velocityX, double velocityY, double damage, String ownerId) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.ownerId = ownerId;
        this.active = true;
        this.lifetime = 0.0;
        
        if (DEBUG_MODE) {
            for (int i = 0; i < MAX_TRAIL_POINTS; i++) {
                trailX[i] = x;
                trailY[i] = y;
            }
        }
    }

    /**
     * Updates the projectile's state for a single frame. 
     * @param delta The time elapsed since the last update, in seconds.
     */
    public void tick(double delta) {
        this.x += velocityX * delta;
        this.y += velocityY * delta;
        this.lifetime += delta;
        
        if (DEBUG_MODE) {
            trailIndex = (trailIndex + 1) % MAX_TRAIL_POINTS;
            trailX[trailIndex] = x;
            trailY[trailIndex] = y;
            if (!trailInitialized && trailIndex == MAX_TRAIL_POINTS - 1) {
                trailInitialized = true;
            }
        }
    }
    
    /**
     * Renders the projectile on the screen. 
     * @param g2d The {@link Graphics2D} context to draw on.
     * @param offsetX The horizontal offset of the camera.
     * @param offsetY The vertical offset of the camera.
     */
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        if (!active) return;
        
        double screenX = x - offsetX;
        double screenY = y - offsetY;
        
        if (DEBUG_MODE) {
            g2d.setColor(DEBUG_TRAIL_COLOR);
            g2d.setStroke(new BasicStroke(2.0f));
            
            if (trailInitialized) {
                for (int i = 0; i < MAX_TRAIL_POINTS - 1; i++) {
                    int idx1 = (trailIndex + i + 1) % MAX_TRAIL_POINTS;
                    int idx2 = (trailIndex + i + 2) % MAX_TRAIL_POINTS;
                    
                    float alpha = 0.8f * (float)i / MAX_TRAIL_POINTS;
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    
                    g2d.drawLine(
                        (int)(trailX[idx1] - offsetX), (int)(trailY[idx1] - offsetY),
                        (int)(trailX[idx2] - offsetX), (int)(trailY[idx2] - offsetY)
                    );
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            }
        }
        
        double angle = Math.atan2(velocityY, velocityX);
        
        g2d.setColor(new Color(255, 100, 30, 60));
        g2d.fillOval((int)(screenX - 6), (int)(screenY - 6), 12, 12);
        
        g2d.setColor(new Color(255, 200, 50));
        
        Path2D.Double projectileShape = new Path2D.Double();
        projectileShape.moveTo(screenX + Math.cos(angle) * 5, screenY + Math.sin(angle) * 5);
        projectileShape.lineTo(screenX + Math.cos(angle + Math.PI/2) * 2, screenY + Math.sin(angle + Math.PI/2) * 2);
        projectileShape.lineTo(screenX - Math.cos(angle) * 3, screenY - Math.sin(angle) * 3);
        projectileShape.lineTo(screenX + Math.cos(angle - Math.PI/2) * 2, screenY + Math.sin(angle - Math.PI/2) * 2);
        projectileShape.closePath();
        
        g2d.fill(projectileShape);
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)(screenX - 1), (int)(screenY - 1), 3, 3);
    }

    /**
     * Gets the current x-coordinate of the projectile.
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the current y-coordinate of the projectile.
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the unique ID of the projectile.
     * @return The projectile's ID string.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the amount of damage this projectile deals.
     * @return The damage value.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Gets the ID of the entity that fired this projectile.
     * @return The owner's ID string.
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * Checks if this projectile is currently active. 
     * @return {@code true} if the projectile is active, {@code false} otherwise.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active state of this projectile.
     * @param active {@code true} to make the projectile active, {@code false} to deactivate it.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Gets the x-component of the projectile's velocity.
     * @return The velocity on the x-axis.
     */
    public double getVelocityX() {
        return velocityX;
    }
    
    /**
     * Gets the y-component of the projectile's velocity.
     * @return The velocity on the y-axis.
     */
    public double getVelocityY() {
        return velocityY;
    }
    
    /**
     * Gets the total time this projectile has been active.
     * @return The lifetime in seconds.
     */
    public double getLifetime() {
        return lifetime;
    }
    
    /**
     * Sets the x-coordinate of the projectile.
     * @param x The new x-coordinate.
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /**
     * Sets the y-coordinate of the projectile.
     * @param y The new y-coordinate.
     */
    public void setY(double y) {
        this.y = y;
    }
    
    /**
     * Sets the x-component of the projectile's velocity.
     * @param velocityX The new velocity on the x-axis.
     */
    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }
    
    /**
     * Sets the y-component of the projectile's velocity.
     * @param velocityY The new velocity on the y-axis.
     */
    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    /**
     * Returns a string representation of the projectile's state.
     * @return A string detailing the projectile's properties.
     */
    @Override
    public String toString() {
        return "Projectile{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", velocityX=" + velocityX +
                ", velocityY=" + velocityY +
                ", damage=" + damage +
                ", ownerId='" + ownerId + '\'' +
                ", active=" + active +
                ", lifetime=" + lifetime +
                '}';
    }
}