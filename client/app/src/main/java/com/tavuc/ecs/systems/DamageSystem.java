package com.tavuc.ecs.systems;

import java.util.List;

import com.tavuc.models.space.Projectile;
import com.tavuc.models.space.Ship;

/**
 * The DamageSystem is responsible for processing interactions that result in damage.
 */
public class DamageSystem {
    
    /**
     * Processes all potential collisions between a list of projectiles and a list of ships.
     * If a collision is detected, damage is applied to the ship, and the projectile is deactivated.
     * @param ships The list of ships to check for collisions.
     * @param projectiles The list of active projectiles to check for collisions.
     */
    public void processCollisions(List<Ship> ships, List<Projectile> projectiles) {
        for (Projectile projectile : projectiles) {
            if (!projectile.isActive()) {
                continue;
            }
            for (Ship ship : ships) {
                if (isColliding(projectile, ship)) {
                    applyDamage(ship, projectile.getDamage());
                    projectile.setActive(false);
                }
            }
        }
    }
    
    /**
     * Applies a specified amount of damage to a ship. 
     * @param ship The ship to receive the damage.
     * @param damage The amount of damage to apply.
     */
    private void applyDamage(Ship ship, double damage) {
        ship.takeDamage(damage);
    }
    
    /**
     * Determines if a projectile and a ship are colliding based on their positions.
     * @param projectile The projectile object.
     * @param ship The ship object.
     * @return {@code true} if the distance between the objects is less than the assumed
     *         collision radius, {@code false} otherwise.
     */
    private boolean isColliding(Projectile projectile, Ship ship) {
        double dx = projectile.getX() - ship.getX();
        double dy = projectile.getY() - ship.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < 64; //TODO: UPDATE TO USE PROPER SHIP MODEL
    }
}