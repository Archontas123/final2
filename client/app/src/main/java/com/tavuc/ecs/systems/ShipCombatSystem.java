package com.tavuc.ecs.systems;

import com.tavuc.Client;
import com.tavuc.ecs.ComponentContainer;
import com.tavuc.ecs.components.HealthComponent;
import com.tavuc.ecs.components.ShieldComponent;
import com.tavuc.models.space.Projectile;
import com.tavuc.models.space.Ship;
import com.tavuc.networking.models.FireRequest;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The ShipCombatSystem handles all client-side combat logic for ships.
 */
public class ShipCombatSystem {

    /**
     * The speed of projectiles in pixels per second. 
     */
    private static final float PROJECTILE_SPEED = 120.0f;
    /**
     * The size of the projectile in pixels for rendering.
     */
    private static final int PROJECTILE_SIZE = 5; 
    /**
     * The base damage a single projectile inflicts upon hitting a target.
     */
    private static final double PROJECTILE_DAMAGE = 10.0;
    /**
     * The amount of damage inflicted when two ships collide.
     */
    private static final double COLLISION_DAMAGE = 25.0;
    /**
     * The maximum damage an explosion can inflict at its epicenter.
     */
    private static final double EXPLOSION_DAMAGE = 30.0;
    /**
     * The radius of an explosion's area of effect in pixels.
     */
    private static final double EXPLOSION_RADIUS = 150.0;
    /**
     * The minimum time in milliseconds between consecutive shots from the player.
     */
    private static final long FIRE_COOLDOWN_MS = 300; 
    
    /**
     * The player's ship instance that this system manages combat for.
     */
    private final Ship playerShip;
    /**
     * A map of all active projectiles in the simulation, keyed by their unique ID.
     */
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();
    /**
     * The timestamp of the last time the player fired a projectile.
     */
    private long lastFireTime = 0;
    /**
     * A list of active explosion effects currently being rendered.
     */
    private final List<ExplosionData> explosions = new CopyOnWriteArrayList<>();
    
    /**
     * Constructs a new ShipCombatSystem for a specific player ship.
     * @param playerShip The ship controlled by the local player.
     */
    public ShipCombatSystem(Ship playerShip) {
        this.playerShip = playerShip;
        this.lastFireTime = 0;
    }
    
    /**
     * Attempts to fire a projectile from the player's ship. If successful, it sends a fire request to the server.
     */
    public void fireProjectile() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime < FIRE_COOLDOWN_MS) {
            long remainingCooldown = FIRE_COOLDOWN_MS - (currentTime - lastFireTime);
            return;
        }
        
        lastFireTime = currentTime;
        
        
        FireRequest request = new FireRequest(
            String.valueOf(Client.getInstance().getPlayerId()),
            playerShip.getX(),
            playerShip.getY(),
            playerShip.getAngle(),
            playerShip.getDx(),
            playerShip.getDy()
        );
        Client.sendFireRequest(request);
    }

    /**
     * Adds a projectile created by another player to the
     * local combat simulation.
     * @param projectile The projectile to add.
     */
    public void addRemoteProjectile(Projectile projectile) {
        projectiles.put(projectile.getId(), projectile);
    }

    /**
     * Updates the state of the combat system. 
     * @param delta The time elapsed since the last update, in seconds.
     * @param otherShips A list of other ships in the game to check for collisions against.
     */
    public void update(double delta, List<Ship> otherShips) {
        updateProjectiles(delta);
        checkShipCollisions(otherShips);
        updateExplosions(delta);
        
        ShieldComponent shield = playerShip.getComponents().getComponent(ShieldComponent.class);
        shield.update((float)delta);
    }
    
    /**
     * Updates the position of all active projectiles and removes any that have
     * gone out of range or become inactive.
     * @param delta The time elapsed since the last update, in seconds.
     */
    private void updateProjectiles(double delta) {
        List<String> projectilesToRemove = new ArrayList<>();

        for (Projectile projectile : projectiles.values()) {
            projectile.tick(delta);

            double distanceFromPlayer = Math.sqrt(
                Math.pow(projectile.getX() - playerShip.getX(), 2) +
                Math.pow(projectile.getY() - playerShip.getY(), 2)
            );

            if (distanceFromPlayer > 1500 || !projectile.isActive()) {
                projectilesToRemove.add(projectile.getId());
            }
        }

        for (String id : projectilesToRemove) {
            projectiles.remove(id);
        }
    }
    
    /**
     * Checks for collisions between the player's ship and other ships and applying appropriate effects if it does.
     * @param otherShips A list of all other ships to check for collisions.
     */
    private void checkShipCollisions(List<Ship> otherShips) {
        for (Ship otherShip : otherShips) {
            if (otherShip.getHealth() <= 0) continue;
            
            double dx = playerShip.getX() - otherShip.getX();
            double dy = playerShip.getY() - otherShip.getY();
            double distance = Math.sqrt(dx*dx + dy*dy);
            
            double minDistance = (playerShip.getWidth() + otherShip.getWidth()) / 2;
            
            if (distance < minDistance) {
                System.out.println("[ShipCombatSystem] Ship collision detected");
                applyDamage(playerShip, COLLISION_DAMAGE);
                
                double angle = Math.atan2(dy, dx);
                double playerMass = 1.0;
                double otherMass = 1.0;
                double forceFactor = 5.0;
                
                double playerImpulseX = Math.cos(angle) * forceFactor;
                double playerImpulseY = Math.sin(angle) * forceFactor;
                playerShip.setDx(playerShip.getDx() + playerImpulseX * (otherMass / playerMass));
                playerShip.setDy(playerShip.getDy() + playerImpulseY * (otherMass / playerMass));
            }
        }
    }
    
    /**
     * Creates an explosion effect at a specified location. 
     * @param x The x-coordinate for the center of the explosion.
     * @param y The y-coordinate for the center of the explosion.
     */
    public void createExplosion(double x, double y) {
        ExplosionData explosion = new ExplosionData(x, y, 1.0); 
        explosions.add(explosion);
        
        List<Ship> nearbyShips = getNearbyShips(x, y, EXPLOSION_RADIUS);
        for (Ship ship : nearbyShips) {
            double distance = Math.sqrt(
                Math.pow(ship.getX() + ship.getWidth()/2 - x, 2) + 
                Math.pow(ship.getY() + ship.getHeight()/2 - y, 2)
            );
            
            double damageMultiplier = 1.0 - (distance / EXPLOSION_RADIUS);
            if (damageMultiplier > 0) applyDamage(ship, EXPLOSION_DAMAGE * damageMultiplier);
        }
    }
    
    /**
     * Finds and returns a list of all ships within a given radius of a point.
     * @param x The x-coordinate of the center of the search area.
     * @param y The y-coordinate of the center of the search area.
     * @param radius The radius of the search area.
     * @return A list of ships found within the specified radius.
     */
    private List<Ship> getNearbyShips(double x, double y, double radius) {
        List<Ship> result = new ArrayList<>();
        
        double playerDistance = Math.sqrt(
            Math.pow(playerShip.getX() + playerShip.getWidth()/2 - x, 2) + 
            Math.pow(playerShip.getY() + playerShip.getHeight()/2 - y, 2)
        );
        if (playerDistance <= radius) {
            result.add(playerShip);
        }
        
        if (Client.currentSpacePanel != null) {
            for (Ship otherShip : Client.currentSpacePanel.getOtherPlayerShips()) {
                double distance = Math.sqrt(
                    Math.pow(otherShip.getX() + otherShip.getWidth()/2 - x, 2) + 
                    Math.pow(otherShip.getY() + otherShip.getHeight()/2 - y, 2)
                );
                if (distance <= radius) {
                    result.add(otherShip);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Updates the state of all active explosion animations, removing them once
     * their duration has elapsed.
     * @param delta The time elapsed since the last update, in seconds.
     */
    private void updateExplosions(double delta) {
        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });
    }
    
    /**
     * Applies a specified amount of damage to a ship. 
     * @param ship The ship to apply damage to.
     * @param damage The amount of damage to apply.
     */
    private void applyDamage(Ship ship, double damage) {
        ship.takeDamage(damage);
    }
    
    /**
     * Gathers rendering information for all active projectiles.
     * @return A list of {@link ProjectileRenderData} objects for the rendering engine.
     */
    public List<ProjectileRenderData> getProjectilesToRender() {
        List<ProjectileRenderData> renderList = new ArrayList<>();
        for (Projectile projectile : projectiles.values()) {
            if (projectile.isActive()) {
                renderList.add(new ProjectileRenderData(
                    projectile.getX(),
                    projectile.getY(),
                    PROJECTILE_SIZE,
                    Color.RED
                ));
            }
        }
        return renderList;
    }
    
    /**
     * Gathers rendering information for all active explosions.
     * @return A list of {@link ExplosionRenderData} objects for the rendering engine.
     */
    public List<ExplosionRenderData> getExplosionsToRender() {
        List<ExplosionRenderData> renderList = new ArrayList<>();
        for (ExplosionData explosion : explosions) {
            renderList.add(new ExplosionRenderData(
                explosion.x,
                explosion.y,
                explosion.getScale(),
                explosion.getAlpha()
            ));
        }
        return renderList;
    }
    
    /**
     * Handles the destruction of the player's ship by creating an explosion at its location.
     */
    public void handlePlayerDestroyed() {
        createExplosion(
            playerShip.getX() + playerShip.getWidth()/2, 
            playerShip.getY() + playerShip.getHeight()/2
        );
    }
    
    /**
     * Gets a list of all active projectiles currently managed by the system.
     * @return A new list containing all active projectiles.
     */
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles.values());
    }

    /**
     * Updates the state of a specific projectile.
     * @param projectileId The unique ID of the projectile to update.
     * @param x The new x-coordinate.
     * @param y The new y-coordinate.
     * @param velocityX The new velocity on the x-axis.
     * @param velocityY The new velocity on the y-axis.
     */
    public void updateProjectile(String projectileId, double x, double y, double velocityX, double velocityY) {
        Projectile p = projectiles.get(projectileId);
        if (p != null) {
            p.setX(x);
            p.setY(y);
            p.setVelocityX(velocityX);
            p.setVelocityY(velocityY);
        }
    }

    /**
     * Removes a projectile from the simulation.
     * @param projectileId The unique ID of the projectile to remove.
     */
    public void removeProjectile(String projectileId) {
        projectiles.remove(projectileId);
    }

    /**
     * Gets the player ship associated with this combat system.
     * @return The player's {@link Ship} instance.
     */
    public Ship getPlayerShip() {
        return playerShip;
    }
    
    /**
     * A data-transfer object that holds all necessary information for rendering a projectile.
     */
    public static class ProjectileRenderData {
        /** The x-coordinate for rendering. */
        public final double x;
        /** The y-coordinate for rendering. */
        public final double y;
        /** The size (diameter) for rendering. */
        public final int size;
        /** The color for rendering. */
        public final Color color;
        
        /**
         * Constructs a new ProjectileRenderData object.
         * @param x The x-coordinate of the projectile.
         * @param y The y-coordinate of the projectile.
         * @param size The size of the projectile.
         * @param color The color of the projectile.
         */
        public ProjectileRenderData(double x, double y, int size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }
    
    /**
     * A data-transfer object that holds all necessary information for rendering an explosion.
     */
    public static class ExplosionRenderData {
        /** The x-coordinate for rendering. */
        public final double x;
        /** The y-coordinate for rendering. */
        public final double y;
        /** The current scale multiplier for the explosion graphic. */
        public final float scale;
        /** The current alpha (transparency) value for the explosion graphic. */
        public final float alpha;
        
        /**
         * Constructs a new ExplosionRenderData object.
         * @param x The x-coordinate of the explosion.
         * @param y The y-coordinate of the explosion.
         * @param scale The current scale of the explosion.
         * @param alpha The current alpha transparency of the explosion.
         */
        public ExplosionRenderData(double x, double y, float scale, float alpha) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.alpha = alpha;
        }
    }

    /**
     * An internal class used to track the state and lifetime of an explosion effect.
     */
    private static class ExplosionData {
        /** The x-coordinate of the explosion's center. */
        private final double x, y;
        /** The total duration of the explosion animation in seconds. */
        private final double duration;
        /** The time elapsed since the explosion was created, in seconds. */
        private double elapsed = 0;
        
        /**
         * Constructs a new ExplosionData object.
         * @param x The x-coordinate of the explosion.
         * @param y The y-coordinate of the explosion.
         * @param duration The total duration of the effect in seconds.
         */
        public ExplosionData(double x, double y, double duration) {
            this.x = x;
            this.y = y;
            this.duration = duration;
        }
        
        /**
         * Advances the explosion's internal timer.
         * @param delta The time elapsed since the last update, in seconds.
         */
        public void update(double delta) {
            elapsed += delta;
        }
        
        /**
         * Checks if the explosion's animation has completed.
         * @return {@code true} if the elapsed time is greater than or equal to the duration, {@code false} otherwise.
         */
        public boolean isFinished() {
            return elapsed >= duration;
        }
        
        /**
         * Calculates the current alpha (transparency) for the explosion, which fades out over time.
         * @return The alpha value, from 1.0  to 0.0.
         */
        public float getAlpha() {
            return (float)(1.0 - (elapsed / duration));
        }
        
        /**
         * Calculates the current scale for the explosion, which grows over its lifetime.
         * @return The scale multiplier for the explosion graphic.
         */
        public float getScale() {
            double progress = elapsed / duration;
            if (progress < 0.3) {
                return (float)(progress / 0.3 * 2.0);
            } else {
                return (float)(2.0 + (progress - 0.3) / 0.7 * 3.0);
            }
        }
    }
}