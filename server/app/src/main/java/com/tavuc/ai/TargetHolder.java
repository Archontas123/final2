package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Interface for enemies capable of holding a target reference.
 */
public interface TargetHolder {
    /** Sets the current target for the enemy. */
    void setTarget(Entity target);
    /** Returns the current target for the enemy. */
    Entity getTarget();
}
