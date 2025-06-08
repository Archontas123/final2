package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

import java.util.List;

/**
 * Very small targeting system used on the server to pick the first
 * available target from a provided list.
 */
public class BasicTargetingSystem implements TargetingSystem {

    private final List<? extends Entity> possibleTargets;

    public BasicTargetingSystem(List<? extends Entity> targets) {
        this.possibleTargets = targets;
    }

    @Override
    public Entity acquireTarget() {
        return possibleTargets.isEmpty() ? null : possibleTargets.get(0);
    }
}
