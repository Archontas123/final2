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
import java.util.concurrent.CopyOnWriteArrayList;


public class ShipCombatSystem {

    private static final float PROJECTILE_SPEED = 15.0f;
    private static final int PROJECTILE_SIZE = 8;
    private static final double PROJECTILE_DAMAGE = 10.0;
    private static final double COLLISION_DAMAGE = 25.0;
    private static final double EXPLOSION_DAMAGE = 50.0;
    private static final double EXPLOSION_RADIUS = 150.0;
    private static final long FIRE_COOLDOWN_MS = 300; 
    
    private final Ship playerShip;
    private final List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    private long lastFireTime = 0;
    private final List<Explosion> explosions = new CopyOnWriteArrayList<>();
    
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
        
        double angle = playerShip.getAngle();
        double shipCenterX = playerShip.getX() + playerShip.getWidth() / 2.0;
        double shipCenterY = playerShip.getY() + playerShip.getHeight() / 2.0;
        
        // FIXED: Spawn projectile in front of ship in the direction it's facing
        // Reduced spawn distance so projectiles start closer to ship (more visible)
        double spawnDistance = 20; // Smaller distance for better visibility
        double spawnX = shipCenterX + Math.sin(angle) * spawnDistance;
        double spawnY = shipCenterY - Math.cos(angle) * spawnDistance;
        
        // FIXED: Velocity should move projectile in the same direction as spawn offset
        double velocityX = Math.sin(angle) * PROJECTILE_SPEED;
        double velocityY = -Math.cos(angle) * PROJECTILE_SPEED; // Negative because Y increases downward
        
        // Add ship momentum
        velocityX += playerShip.getDx() * 0.5;
        velocityY += playerShip.getDy() * 0.5;
        
        // Create and add the projectile to the local list
        Projectile projectile = new Projectile(
            spawnX, spawnY, 
            velocityX, velocityY, 
            PROJECTILE_DAMAGE, 
            String.valueOf(Client.getInstance().getPlayerId())
        );
        projectiles.add(projectile);
        
        System.out.println("[ShipCombatSystem] Fired projectile:");
        System.out.println("  - Ship center: (" + shipCenterX + ", " + shipCenterY + ")");
        System.out.println("  - Ship angle: " + Math.toDegrees(angle) + " degrees");
        System.out.println("  - Spawn at: (" + spawnX + ", " + spawnY + ")");
        System.out.println("  - Velocity: (" + velocityX + ", " + velocityY + ")");
        System.out.println("  - Total projectiles: " + projectiles.size());
        
        // Send fire request to server with proper ship data
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
        projectiles.add(projectile);
        System.out.println("[ShipCombatSystem] Added remote projectile from player " + projectile.getOwnerId() + ". Total projectiles: " + projectiles.size());
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
        List<Projectile> projectilesToRemove = new ArrayList<>();

        for (Projectile projectile : projectiles) {
            projectile.tick(delta);

            double distanceFromPlayer = Math.sqrt(
                Math.pow(projectile.getX() - playerShip.getX(), 2) +
                Math.pow(projectile.getY() - playerShip.getY(), 2)
            );

            if (distanceFromPlayer > 1500 || !projectile.isActive()) {
                projectilesToRemove.add(projectile);
            }
        }

        // Remove projectiles outside the loop
        if (!projectilesToRemove.isEmpty()) {
            projectiles.removeAll(projectilesToRemove);
            System.out.println("[ShipCombatSystem] Removed " + projectilesToRemove.size() + " projectiles. Remaining: " + projectiles.size());
        }
    }
    
    /**
     * Checks for collisions between projectiles and ships.
     * @param otherShips a List of all the ships with which collision should be checked
     */
    private void checkProjectileCollisions(List<Ship> otherShips) {
        List<Ship> allShips = new ArrayList<>(otherShips);
        allShips.add(playerShip);
        
        for (Projectile projectile : projectiles) {
            if (!projectile.isActive()) continue;
            String ownerId = projectile.getOwnerId();
            
            for (Ship ship : allShips) {
                if (ownerId.equals(String.valueOf(Client.getInstance().getPlayerId())) && ship == playerShip) continue;
                
                //TODO: Improve Collision Check with actual Hitbox's
                double dx = projectile.getX() - (ship.getX() + ship.getWidth()/2);
                double dy = projectile.getY() - (ship.getY() + ship.getHeight()/2);
                double distance = Math.sqrt(dx*dx + dy*dy);
                
                if (distance < ship.getWidth()/2) {
                    applyDamage(ship, projectile.getDamage());
                    projectile.setActive(false);
                    if (ship.getHealth() <= 0) createExplosion(ship.getX() + ship.getWidth()/2, ship.getY() + ship.getHeight()/2);
                    break; 
                }
            }
        }
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
    private void createExplosion(double x, double y) {
        Explosion explosion = new Explosion(x, y, 1.0); 
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
        Iterator<Explosion> iterator = explosions.iterator();
        while (iterator.hasNext()) {
            Explosion explosion = iterator.next();
            explosion.update(delta);
            
            if (explosion.isFinished()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Applies damage to a ship, considering shields first.
     * @param Ship the ship to which damage should be applied
     * @param damage the damage to apply to the Ship
     */
    private void applyDamage(Ship ship, double damage) {
        ship.takeDamage(damage);
    }
    
    /**
     * Renders all projectiles and explosions.
     * @param g2d Graphics2D context for rendering
     * @param offsetX Camera offset X
     * @param offsetY Camera offset Y
     */
    public void render(Graphics2D g2d, double offsetX, double offsetY) {
        for (Projectile projectile : projectiles) {
            System.out.println("RENDERING PROJECTILE" + projectile.toString());
            //if (!projectile.isActive()) continue;
            
            double screenX = projectile.getX() - offsetX;
            double screenY = projectile.getY() - offsetY;
            System.out.println(screenX + "," + screenY);
            
            g2d.setColor(Color.RED);
            g2d.fillOval((int)(screenX - PROJECTILE_SIZE/2), 
                         (int)(screenY - PROJECTILE_SIZE/2), 
                         PROJECTILE_SIZE, PROJECTILE_SIZE);
        }
        
        for (Explosion explosion : explosions) {
            double screenX = explosion.x - offsetX;
            double screenY = explosion.y - offsetY;
            
            float alpha = explosion.getAlpha();
            float scale = explosion.getScale();
            
            AffineTransform oldTransform = g2d.getTransform();
            
            g2d.translate(screenX, screenY);
            g2d.scale(scale, scale);
            
            Color[] explosionColors = {
                new Color(1.0f, 1.0f, 0.2f, alpha * 0.8f),
                new Color(1.0f, 0.5f, 0.0f, alpha * 0.6f),
                new Color(1.0f, 0.2f, 0.0f, alpha * 0.4f)
            };
            
            int[] sizes = {30, 50, 70};
            
            for (int i = 0; i < explosionColors.length; i++) {
                g2d.setColor(explosionColors[i]);
                g2d.fillOval(-sizes[i]/2, -sizes[i]/2, sizes[i], sizes[i]);
            }
            
            g2d.setTransform(oldTransform);
        }
    }
    
    public Ship getPlayerShip() {
        return playerShip;
    }

    
    /**
     * Handles a player ship being destroyed.
     */
    public void handlePlayerDestroyed() {
        createExplosion(
            playerShip.getX() + playerShip.getWidth()/2, 
            playerShip.getY() + playerShip.getHeight()/2
        );
        
        //TODO: ADD FURTHER HANDLING
    }
    
    /**
     * Gets all active projectiles.
     * @return A list of all Projectiles
     */
    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    /** Data used for rendering a projectile. */
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

    /** Data used for rendering an explosion. */
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
     * Returns a list of projectile render data for drawing.
     */
    public List<ProjectileRenderData> getProjectilesToRender() {
        List<ProjectileRenderData> renderData = new ArrayList<>();
        for (Projectile p : projectiles) {
            renderData.add(new ProjectileRenderData(p.getX(), p.getY(), PROJECTILE_SIZE, Color.RED));
        }
        return renderData;
    }

    /**
     * Returns a list of explosion render data for drawing.
     */
    public List<ExplosionRenderData> getExplosionsToRender() {
        List<ExplosionRenderData> renderData = new ArrayList<>();
        for (Explosion e : explosions) {
            renderData.add(new ExplosionRenderData(e.x, e.y, e.getScale(), e.getAlpha()));
        }
        return renderData;
    }


    private static class Explosion {
        private final double x, y;
        private final double duration;
        private double elapsed = 0;
        
        /**
         * A constructor for the Explosion class
         * @param x the X coordinate of the Explosion
         * @param y the Y coordinate of the Explosion
         * @param duration how long the Explosion should last
         */
        public Explosion(double x, double y, double duration) {
            this.x = x;
            this.y = y;
            this.duration = duration;
        }
        
        /**
         * 
         * @param delta Time passed since last update in seconds
         */
        public void update(double delta) {
            elapsed += delta;
        }
        
        /**
         * Check if the Explosion is finished
         * @return true if its finished false if its not
         */
        public boolean isFinished() {
            return elapsed >= duration;
        }
        

        /**
         * Gets the alpha value for the explosion effect
         * @return Alpha transparency value
         */
        public float getAlpha() {
            return (float)(1.0 - (elapsed / duration));
        }
        
        /**
         * Gets the scale value for the explosion effect
         * @return Scale factor for rendering
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