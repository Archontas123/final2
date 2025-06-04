
package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.geom.Path2D;

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
    
    // Debug variables
    private static final boolean DEBUG_MODE = true;
    private static final Color DEBUG_TRAIL_COLOR = new Color(255, 0, 0, 100);
    private static final int MAX_TRAIL_POINTS = 20;
    private double[] trailX = new double[MAX_TRAIL_POINTS];
    private double[] trailY = new double[MAX_TRAIL_POINTS];
    private int trailIndex = 0;
    private boolean trailInitialized = false;

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
        
        // Initialize trail with current position
        if (DEBUG_MODE) {
            for (int i = 0; i < MAX_TRAIL_POINTS; i++) {
                trailX[i] = x;
                trailY[i] = y;
            }
        }
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
        
        // Update trail
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
     * Draws the projectile and its debug trail.
     *
     * @param g2d The graphics context
     * @param offsetX The camera X offset
     * @param offsetY The camera Y offset
     */
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        if (!active) return;
        
        double screenX = x - offsetX;
        double screenY = y - offsetY;
        
        // Draw debug trail
        if (DEBUG_MODE) {
            g2d.setColor(DEBUG_TRAIL_COLOR);
            g2d.setStroke(new BasicStroke(2.0f));
            
            // Draw motion trail
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
        
        // Draw projectile core with a glow effect
        double angle = Math.atan2(velocityY, velocityX);
        
        // Draw projectile glow
        g2d.setColor(new Color(255, 100, 30, 60));
        g2d.fillOval((int)(screenX - 6), (int)(screenY - 6), 12, 12);
        
        // Draw projectile main body
        g2d.setColor(new Color(255, 200, 50));
        
        // Create elongated projectile shape based on velocity
        Path2D.Double projectileShape = new Path2D.Double();
        projectileShape.moveTo(screenX + Math.cos(angle) * 5, screenY + Math.sin(angle) * 5);
        projectileShape.lineTo(screenX + Math.cos(angle + Math.PI/2) * 2, screenY + Math.sin(angle + Math.PI/2) * 2);
        projectileShape.lineTo(screenX - Math.cos(angle) * 3, screenY - Math.sin(angle) * 3);
        projectileShape.lineTo(screenX + Math.cos(angle - Math.PI/2) * 2, screenY + Math.sin(angle - Math.PI/2) * 2);
        projectileShape.closePath();
        
        g2d.fill(projectileShape);
        
        // Draw bright core
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int)(screenX - 1), (int)(screenY - 1), 3, 3);
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

    @Override
    public String toString() {
        return "Projectile{" +
                "x=" + x +
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
