package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Simplified suppression behavior used by server side troopers.
 */
public class SuppressionBehavior {
    private double cooldown = 0;

    public void update(double deltaTime) {
        if (cooldown > 0) {
            cooldown -= deltaTime;
        }
    }

    public void suppress(Entity attacker, Entity target) {
        if (cooldown <= 0 && target != null) {
            target.takeDamage(0);
            cooldown = 0.5;
        }
    }
}
