package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Handles very simple suppression fire logic for troopers.
 */
public class SuppressionBehavior {
    private double cooldown = 0;

    /** Update cooldown timer. */
    public void update(double deltaTime) {
        if (cooldown > 0) {
            cooldown -= deltaTime;
        }
    }

    /**
     * Perform suppression fire on the target if possible.
     */
    public void suppress(Entity attacker, Entity target) {
        if (cooldown <= 0 && target != null) {
            // No real damage here, but could trigger effects in a real game.
            target.takeDamage(0);
            cooldown = 0.5; // fire once every half second
        }
    }
}
