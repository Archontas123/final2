package com.tavuc.models.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tavuc.ecs.systems.ShipCombatSystem;


/**
 * Represents the space environment containing planets, ships, and combat elements.
 */
public class Space {
    /**
     * List of all planets in space
     */
    private List<Planet> planets;
    /**
     * List of all ships in space
     */
    private List<Ship> ships;
    /**
     * List of all active projectiles
     */
    private List<Projectile> projectiles;
    /**
     * List of all current explosions
     */
    private List<Explosion> explosions;
    /**
     * The combat system
     */
    private ShipCombatSystem combatSystem;
    /**
     * If the combat System is enabled or not (for debugging purposes)
     */
    private boolean combatEnabled = true;

    /**
     * Constructor for Space.
     * Initializes the collections for planets, ships, and combat elements.
     */
    public Space() {
        this.planets = new ArrayList<>();
        this.ships = new ArrayList<>();
        this.projectiles = new CopyOnWriteArrayList<>();
        this.explosions = new ArrayList<>();
    }

    /**
     * Sets the combat system for this space.
     * @param combatSystem The ShipCombatSystem to use
     */
    public void setCombatSystem(ShipCombatSystem combatSystem) {
        this.combatSystem = combatSystem;
    }

    /**
     * Gets the combat system for this space.
     * @return The ShipCombatSystem
     */
    public ShipCombatSystem getCombatSystem() {
        return combatSystem;
    }

    /**
     * Sets whether combat is enabled in this space.
     * @param enabled True to enable combat, false to disable
     */
    public void setCombatEnabled(boolean enabled) {
        this.combatEnabled = enabled;
    }

    /**
     * Checks if combat is enabled in this space.
     * @return True if combat is enabled, false otherwise
     */
    public boolean isCombatEnabled() {
        return combatEnabled;
    }

    /**
     * Adds a planet to this space.
     * @param planet The planet to add
     */
    public void addPlanet(Planet planet) {
        this.planets.add(planet);
    }

    /**
     * Gets all planets in this space.
     * @return List of planets
     */
    public List<Planet> getPlanets() {
        return this.planets;
    }

    /**
     * Removes a planet from this space.
     * @param planet The planet to remove
     */
    public void removePlanet(Planet planet) {
        this.planets.remove(planet);
    }

    /**
     * Adds a ship to this space.
     * @param ship The ship to add
     */
    public void addShip(Ship ship) {
        this.ships.add(ship);
    }

    /**
     * Gets all ships in this space.
     * @return List of ships
     */
    public List<Ship> getShips() {
        return this.ships;
    }

    /**
     * Removes a ship from this space.
     * @param ship The ship to remove
     */
    public void removeShip(Ship ship) {
        this.ships.remove(ship);
    }

    /**
     * Adds a projectile to this space.
     * @param projectile The projectile to add
     */
    public void addProjectile(Projectile projectile) {
        this.projectiles.add(projectile);
    }

    /**
     * Gets all projectiles in this space.
     * @return List of projectiles
     */
    public List<Projectile> getProjectiles() {
        return this.projectiles;
    }

    /**
     * Removes a projectile from this space.
     * @param projectile The projectile to remove
     */
    public void removeProjectile(Projectile projectile) {
        this.projectiles.remove(projectile);
    }

    /**
     * Adds an explosion effect to this space.
     * @param explosion The explosion to add
     */
    public void addExplosion(Explosion explosion) {
        this.explosions.add(explosion);
    }

    /**
     * Gets all explosions in this space.
     * @return List of explosions
     */
    public List<Explosion> getExplosions() {
        return this.explosions;
    }

    /**
     * Updates all entities in this space.
     * @param deltaTime Time passed since the last update in seconds
     */
    public void update(double deltaTime) {
        // Update planets
        for (Planet planet : new ArrayList<>(planets)) {
            planet.update(deltaTime);
            
        }
        
        // Update ships and check for destroyed ships
        Iterator<Ship> shipIterator = new ArrayList<>(ships).iterator();
        while (shipIterator.hasNext()) {
            Ship ship = shipIterator.next();
            
            if (ship.isDestroyed()) {
                // Handle ship destruction if not already handled by combat system
                if (combatSystem != null && ship == combatSystem.getPlayerShip()) {
                    combatSystem.handlePlayerDestroyed();
                } else {
                    // Create explosion for destroyed ship
                    Explosion explosion = new Explosion(
                        ship.getX() + ship.getWidth()/2,
                        ship.getY() + ship.getHeight()/2,
                        1.0); // 1 second duration
                    explosions.add(explosion);
                    
                    // Remove the ship
                    ships.remove(ship);
                }
            } else {
                // Update active ships
                ship.update(deltaTime);
            }
        }
        
        // Update projectiles
        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.tick(deltaTime);
            
            // Remove inactive or expired projectiles
            if (!projectile.isActive() || projectile.getLifetime() > 5.0) {
                projectileIterator.remove();
            }
        }
        
        // Update explosions
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update(deltaTime);
            
            // Remove finished explosions
            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }
        
        // Update combat system if enabled and available
        if (combatEnabled && combatSystem != null) {
            combatSystem.update(deltaTime, ships);
        }
    }
    
    /**
     * Explosion effect class for visual representation of explosions.
     */
    public static class Explosion {
        private final double x, y;
        private final double duration;
        private double elapsed = 0;
        
        /**
         * Constructor for Explosion.
         * @param x X coordinate of explosion center
         * @param y Y coordinate of explosion center
         * @param duration Duration in seconds
         */
        public Explosion(double x, double y, double duration) {
            this.x = x;
            this.y = y;
            this.duration = duration;
        }
        
        /**
         * Updates the explosion effect.
         * @param deltaTime Time passed since the last update in seconds
         */
        public void update(double deltaTime) {
            elapsed += deltaTime;
        }
        
        /**
         * Checks if the explosion effect has finished.
         * @return True if finished, false otherwise
         */
        public boolean isFinished() {
            return elapsed >= duration;
        }
        
        /**
         * Gets the X coordinate of the explosion.
         * @return X coordinate
         */
        public double getX() {
            return x;
        }
        
        /**
         * Gets the Y coordinate of the explosion.
         * @return Y coordinate
         */
        public double getY() {
            return y;
        }
        
        /**
         * Gets the alpha transparency value for rendering.
         * @return Alpha value between 0.0 and 1.0
         */
        public float getAlpha() {
            return (float)(1.0 - (elapsed / duration));
        }
        
        /**
         * Gets the scale factor for rendering.
         * @return Scale factor
         */
        public float getScale() {
            // Start small, expand, then fade out
            double progress = elapsed / duration;
            if (progress < 0.3) {
                return (float)(progress / 0.3 * 2.0);
            } else {
                return (float)(2.0 + (progress - 0.3) / 0.7 * 3.0);
            }
        }
    }
}