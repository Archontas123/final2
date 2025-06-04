package com.tavuc.ecs.systems;

import com.tavuc.ecs.components.*;
import com.tavuc.models.space.Projectile;
import com.tavuc.models.space.Ship;
import java.util.List;

public class DamageSystem {
    
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
    
    private void applyDamage(Ship ship, double damage) {
        ship.takeDamage(damage);
    }
    
    private boolean isColliding(Projectile projectile, Ship ship) {
        // Simple collision detection
        double dx = projectile.getX() - ship.getX();
        double dy = projectile.getY() - ship.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < 64; // Assuming ship radius of 64
    }
}
