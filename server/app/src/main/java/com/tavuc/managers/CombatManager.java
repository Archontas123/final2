package com.tavuc.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.tavuc.models.space.BaseShip;
import com.tavuc.models.space.PlayerShip;
import com.tavuc.models.space.ProjectileEntity;
import com.tavuc.networking.ClientSession;
import com.tavuc.networking.models.ProjectileSpawnedBroadcast;
import com.tavuc.networking.models.ProjectileUpdateBroadcast;
import com.tavuc.networking.models.ProjectileRemovedBroadcast;
import com.tavuc.networking.models.ShipDamagedBroadcast;
import com.tavuc.networking.models.ShipDestroyedBroadcast;

/**
 * Manages ship combat on the server side.
 * Handles projectiles, collisions, and damage.
 */
public class CombatManager {
    private static final float PROJECTILE_SPEED = 25.0f;
    private static final int PROJECTILE_WIDTH = 8;
    private static final int PROJECTILE_HEIGHT = 8;
    private static final float PROJECTILE_DAMAGE = 10.0f;
    private static final float COLLISION_DAMAGE = 25.0f;
    private static final float EXPLOSION_DAMAGE = 50.0f;
    private static final float EXPLOSION_RADIUS = 150.0f;
    private static final long FIRE_COOLDOWN_MS = 300;
    
    // Collection of active projectiles
    private final Map<String, ProjectileEntity> activeProjectiles = new ConcurrentHashMap<>();
    
    // Reference to network manager for broadcasting
    private final NetworkManager networkManager;
    
    // Map of player IDs to their last fire time
    private final Map<String, Long> lastFireTimes = new ConcurrentHashMap<>();
    
    public CombatManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }
    
    /**
     * Process a fire request from a player.
     * 
     * @param playerId The ID of the player who fired
     * @param clientSession The client session of the player
     * @return True if the fire request was processed, false if on cooldown
     */
    public boolean processFireRequest(String playerId, ClientSession clientSession) {
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        Long lastFireTime = lastFireTimes.get(playerId);
        if (lastFireTime != null && currentTime - lastFireTime < FIRE_COOLDOWN_MS) {
            return false; // Still on cooldown
        }
        
        // Update last fire time
        lastFireTimes.put(playerId, currentTime);
        
        // Get player ship
        PlayerShip playerShip = networkManager.getPlayerShip(Integer.parseInt(playerId));
        if (playerShip == null) {
            return false; // Player ship not found
        }
        
        // Calculate projectile spawn position and velocity
        float shipX = playerShip.getX();
        float shipY = playerShip.getY();
        float shipAngle = playerShip.getOrientation();
        
        // Calculate spawn position at the ship's front
        float spawnDistance = playerShip.getHeight() / 2.0f;
        float spawnX = shipX + (float)(Math.sin(shipAngle) * spawnDistance);
        float spawnY = shipY - (float)(Math.cos(shipAngle) * spawnDistance);
        
        // Calculate velocity
        float velocityX = (float)(Math.sin(shipAngle) * PROJECTILE_SPEED);
        float velocityY = (float)(-Math.cos(shipAngle) * PROJECTILE_SPEED);
        
        // Add ship's velocity (scaled down)
        velocityX += playerShip.getVelocityX() * 0.5f;
        velocityY += playerShip.getVelocityY() * 0.5f;
        
        // Create projectile
        String projectileId = "proj_" + UUID.randomUUID().toString();
        ProjectileEntity projectile = new ProjectileEntity(
            projectileId, 
            spawnX, 
            spawnY, 
            PROJECTILE_WIDTH, 
            PROJECTILE_HEIGHT,
            shipAngle, 
            velocityX,
            velocityY,
            PROJECTILE_DAMAGE,
            playerId
        );
        
        // Add projectile to active projectiles
        activeProjectiles.put(projectileId, projectile);
        
        // Broadcast projectile to all players
        ProjectileSpawnedBroadcast broadcast = new ProjectileSpawnedBroadcast(
            projectileId,
            (int)spawnX,
            (int)spawnY,
            PROJECTILE_WIDTH,
            PROJECTILE_HEIGHT,
            shipAngle,
            (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY),
            velocityX,
            velocityY,
            PROJECTILE_DAMAGE,
            playerId
        );
        
        networkManager.broadcastMessageToAllActiveSessions(broadcast);
        
        return true;
    }
    
    /**
     * Updates all projectiles, checks for collisions, and processes damage.
     * 
     * @param deltaTime Time passed since last update in seconds
     */
    public void update(float deltaTime) {
        Iterator<ProjectileEntity> projectileIterator = activeProjectiles.values().iterator();
        while (projectileIterator.hasNext()) {
            ProjectileEntity projectile = projectileIterator.next();
            projectile.update(deltaTime);

            boolean removed = false;

            if (projectile.getLifetime() > 15.0f) {
                projectileIterator.remove();
                removed = true;
            } else if (checkProjectileCollisions(projectile)) {
                projectileIterator.remove();
                removed = true;
            }

            if (removed) {
                ProjectileRemovedBroadcast rm = new ProjectileRemovedBroadcast(projectile.getId());
                networkManager.broadcastMessageToAllActiveSessions(rm);
            } else {
                ProjectileUpdateBroadcast up = new ProjectileUpdateBroadcast(
                    projectile.getId(),
                    projectile.getX(),
                    projectile.getY(),
                    projectile.getVelocityX(),
                    projectile.getVelocityY()
                );
                networkManager.broadcastMessageToAllActiveSessions(up);
            }
        }
    }
    
    /**
     * Check if a projectile has collided with any ships.
     * 
     * @param projectile The projectile to check
     * @return True if the projectile hit something
     */
    private boolean checkProjectileCollisions(ProjectileEntity projectile) {
        // Get all ships
        Collection<BaseShip> ships = networkManager.getActiveEntityShips().values();
        
        for (BaseShip ship : ships) {
            // Skip if ship is the owner of the projectile
            if (ship instanceof PlayerShip) {
                PlayerShip playerShip = (PlayerShip) ship;
                if (playerShip.getPlayerId().equals(projectile.getOwnerId())) {
                    continue;
                }
            }
            
            // Simple collision check
            float distance = (float) Math.sqrt(
                Math.pow(ship.getX() - projectile.getX(), 2) +
                Math.pow(ship.getY() - projectile.getY(), 2)
            );
            
            // If distance is less than ship radius + projectile radius
            if (distance < 40.0f) { // Simple hit detection
                // Apply damage
                applyDamageToShip(ship, projectile.getDamage(), projectile.getOwnerId());
                
                // Check if ship is destroyed
                if (ship.getHealth() <= 0) {
                    handleShipDestroyed(ship);
                }
                
                return true; // Projectile hit something
            }
        }
        
        return false; // No collision
    }
    
    /**
     * Apply damage to a ship and broadcast the damage event.
     * 
     * @param ship The ship to damage
     * @param damage Amount of damage to apply
     * @param damageDealerId ID of the entity that dealt the damage
     */
    public void applyDamageToShip(BaseShip ship, float damage, String damageDealerId) {
        float previousHealth = ship.getHealth();
        ship.takeDamage(damage);
        
        // Broadcast damage
        if (ship instanceof PlayerShip) {
            PlayerShip playerShip = (PlayerShip) ship;
            ShipDamagedBroadcast broadcast = new ShipDamagedBroadcast(
                playerShip.getPlayerId(),
                damage,
                ship.getHealth(),
                ship.getMaxHealth(),
                damageDealerId
            );
            
            networkManager.broadcastMessageToAllActiveSessions(broadcast);
        }
    }
    
    /**
     * Handle a ship being destroyed.
     * 
     * @param ship The ship that was destroyed
     */
    private void handleShipDestroyed(BaseShip ship) {
        // Only handle player ships for now
        if (ship instanceof PlayerShip) {
            PlayerShip playerShip = (PlayerShip) ship;
            
            // Broadcast ship destroyed
            ShipDestroyedBroadcast broadcast = new ShipDestroyedBroadcast(
                playerShip.getPlayerId(),
                playerShip.getX(),
                playerShip.getY()
            );
            
            networkManager.broadcastMessageToAllActiveSessions(broadcast);
            
            // Create explosion and damage nearby ships
            List<BaseShip> nearbyShips = getNearbyShips(ship.getX(), ship.getY(), EXPLOSION_RADIUS);
            for (BaseShip nearbyShip : nearbyShips) {
                if (nearbyShip == ship) continue; // Skip the ship that exploded
                
                // Calculate distance
                float distance = (float) Math.sqrt(
                    Math.pow(nearbyShip.getX() - ship.getX(), 2) +
                    Math.pow(nearbyShip.getY() - ship.getY(), 2)
                );
                
                // Damage falls off with distance
                float damageMultiplier = 1.0f - (distance / EXPLOSION_RADIUS);
                if (damageMultiplier > 0) {
                    applyDamageToShip(
                        nearbyShip, 
                        EXPLOSION_DAMAGE * damageMultiplier,
                        playerShip.getPlayerId()
                    );
                }
            }
        }
    }
    
    /**
     * Get all ships near a point.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param radius Search radius
     * @return List of ships within the radius
     */
    private List<BaseShip> getNearbyShips(float x, float y, float radius) {
        List<BaseShip> result = new ArrayList<>();
        Collection<BaseShip> allShips = networkManager.getActiveEntityShips().values();
        
        for (BaseShip ship : allShips) {
            float distance = (float) Math.sqrt(
                Math.pow(ship.getX() - x, 2) +
                Math.pow(ship.getY() - y, 2)
            );
            
            if (distance <= radius) {
                result.add(ship);
            }
        }
        
        return result;
    }
    
    /**
     * Handle a ship-to-ship collision.
     * 
     * @param ship1 First ship
     * @param ship2 Second ship
     */
    public void handleShipCollision(BaseShip ship1, BaseShip ship2) {
        // Apply damage to both ships
        applyDamageToShip(ship1, COLLISION_DAMAGE, ship2 instanceof PlayerShip ? ((PlayerShip)ship2).getPlayerId() : "system");
        applyDamageToShip(ship2, COLLISION_DAMAGE, ship1 instanceof PlayerShip ? ((PlayerShip)ship1).getPlayerId() : "system");
        
        // Check if either ship is destroyed
        if (ship1.getHealth() <= 0) {
            handleShipDestroyed(ship1);
        }
        if (ship2.getHealth() <= 0) {
            handleShipDestroyed(ship2);
        }
    }
}