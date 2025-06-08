package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Picks a target for an enemy.
 */
public interface TargetingSystem {
    Entity acquireTarget();
}
