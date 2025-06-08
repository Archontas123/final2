package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Picks a target for an enemy on the server.
 */
public interface TargetingSystem {
    Entity acquireTarget();
}
