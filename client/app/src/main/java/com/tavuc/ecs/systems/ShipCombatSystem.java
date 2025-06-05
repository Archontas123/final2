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
 * The ShipCombatSystem handles combat logic between ships and projectiles.
 * Instead of drawing directly, it provides render data for the SpacePanel to draw.
 */
public class ShipCombatSystem {

    // Keep in sync with server projectile speed for prediction
    // Drastically increased so projectiles cannot be outrun by the ship
    private static final float PROJECTILE_SPEED = 400.0f;
    private static final int PROJECTILE_SIZE = 12; // Increased size for better visibility
    private static final double PROJECTILE_DAMAGE = 10.0;
    private static final double COLLISION_DAMAGE = 25.0;
    private static final double EXPLOSION_DAMAGE = 50.0;
    private static final double EXPLOSION_RADIUS = 150.0;
    private static final long FIRE_COOLDOWN_MS = 300; 
    
    private final Ship playerShip;
    private final Map<String, Projectile> projectiles = new ConcurrentHashMap<>();
    private long lastFireTime = 0;
    private final List<ExplosionData> explosions = new CopyOnWriteArrayList<>();
    
    public ShipCombatSystem(Ship playerShip) {
        this.playerShip = playerShip;
        this.lastFireTime = 0;
    }
    
    /**
     * Attempts to fire a projectile from the player ship.
     * Respects cooldown and sends fire request to server.
     */
    public void fireProjectile() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime < FIRE_COOLDOWN_MS) {
            long remainingCooldown = FIRE_COOLDOWN_MS - (currentTime - lastFireTime);
            System.out.println("[ShipCombatSystem] Weapon on cooldown for " + remainingCooldown + "ms");
            return;
        }
        
        lastFireTime = currentTime;
        
        System.out.println("[ShipCombatSystem] Fire request sent to server");
        
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
     * Adds a projectile created by another player to the combat system.
     * @param projectile The projectile to add
     */
    public void addRemoteProjectile(Projectile projectile) {
        projectiles.put(projectile.getId(), projectile);
        System.out.println("[ShipCombatSystem] Remote projectile added at: " + projectile.getX() + "," + projectile.getY());
    }

    /**
     * Updates all projectiles, explosions, and handles collisions.
     * @param delta Time passed since last update in seconds
     * @param otherShips List of other player ships to check for collisions
     */
    public void update(double delta, List<Ship> otherShips) {
        updateProjectiles(delta);
        checkProjectileCollisions(otherShips);
        checkShipCollisions(otherShips);
        updateExplosions(delta);
        
        ShieldComponent shield = playerShip.getComponents().getComponent(ShieldComponent.class);
        shield.update((float)delta);
    }
    
    /**
     * Updates all projectiles positions and removes ones that have exceeded lifetime.
     * @param delta Time passed since last update in seconds
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

        // Remove projectiles outside the loop
        for (String id : projectilesToRemove) {
            projectiles.remove(id);
        }
    }
    
    /**
     * Checks for collisions between projectiles and ships.
     * @param otherShips a List of all the ships with which collision should be checked
     */
    private void checkProjectileCollisions(List<Ship> otherShips) {
        // Collision handling now occurs on the server. Client no longer processes projectile hits.
    }
    
    /**
     * Checks for collisions between ships and handles damage.
     * @param otherShips a List of all the ships with which collision should be checked
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
     * Creates an explosion effect at the specified location.
     * @param x The X coordinate at which to create the explosion effect
     * @param y the Y coordinate at which to create the explosion effect
     */
    public void createExplosion(double x, double y) {
        ExplosionData explosion = new ExplosionData(x, y, 1.0); 
        explosions.add(explosion);
        System.out.println("[ShipCombatSystem] Explosion created at: " + x + "," + y);
        
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
     * Returns a list of ships near the specified point.
     * @param x The X coordinate of the point at which to return the list of ships near
     * @param y The Y coordinate of the point at which to return the list of ships near
     * @param radius how close the ship has to be to be retrieved
     * @return A list of ships in radius around point (x,y)
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
     * Updates all explosion effects.
     * @param delta Time passed since last update in seconds
     */
    private void updateExplosions(double delta) {
        explosions.removeIf(explosion -> {
            explosion.update(delta);
            return explosion.isFinished();
        });
    }
    
    /**
     * Applies damage to a ship, considering shields first.
     * @param ship the ship to which damage should be applied
     * @param damage the damage to apply to the ship
     */
    private void applyDamage(Ship ship, double damage) {
        ship.takeDamage(damage);
    }
    
    /**
     * Gets all projectiles that should be rendered
     * @return List of projectiles to render
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
     * Gets all explosions that should be rendered
     * @return List of explosions to render
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
     * Handles a player ship being destroyed.
     */
    public void handlePlayerDestroyed() {
        createExplosion(
            playerShip.getX() + playerShip.getWidth()/2, 
            playerShip.getY() + playerShip.getHeight()/2
        );
    }
    
    /**
     * Gets all active projectiles.
     * @return A list of all Projectiles
     */
    public List<Projectile> getProjectiles() {
        return new ArrayList<>(projectiles.values());
    }

    public void updateProjectile(String projectileId, double x, double y, double velocityX, double velocityY) {
        Projectile p = projectiles.get(projectileId);
        if (p != null) {
            p.setX(x);
            p.setY(y);
            p.setVelocityX(velocityX);
            p.setVelocityY(velocityY);
        }
    }

    public void removeProjectile(String projectileId) {
        projectiles.remove(projectileId);
    }

    public Ship getPlayerShip() {
        return playerShip;
    }
    
    /**
     * Class containing data needed to render a projectile
     */
    public static class ProjectileRenderData {
        public final double x;
        public final double y;
        public final int size;
        public final Color color;
        
        public ProjectileRenderData(double x, double y, int size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }
    
    /**
     * Class containing data needed to render an explosion
     */
    public static class ExplosionRenderData {
        public final double x;
        public final double y;
        public final float scale;
        public final float alpha;
        
        public ExplosionRenderData(double x, double y, float scale, float alpha) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.alpha = alpha;
        }
    }

    /**
     * Internal class for tracking explosion data
     */
    private static class ExplosionData {
        private final double x, y;
        private final double duration;
        private double elapsed = 0;
        
        public ExplosionData(double x, double y, double duration) {
            this.x = x;
            this.y = y;
            this.duration = duration;
        }
        
        public void update(double delta) {
            elapsed += delta;
        }
        
        public boolean isFinished() {
            return elapsed >= duration;
        }
        
        public float getAlpha() {
            return (float)(1.0 - (elapsed / duration));
        }
        
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