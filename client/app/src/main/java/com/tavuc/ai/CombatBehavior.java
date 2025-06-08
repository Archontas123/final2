package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Encapsulates how an enemy fights.
 */
public interface CombatBehavior {
    /**
     * Perform an attack against the given target.
     */
    void performAttack(Entity target);
}
