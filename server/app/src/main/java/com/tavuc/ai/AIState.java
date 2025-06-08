package com.tavuc.ai;

/**
 * Possible states for an enemy AI on the server side.
 */
public enum AIState {
    SPAWNING,
    PATROLLING,
    SEARCHING,
    PURSUING,
    ATTACKING,
    RETREATING,
    FLANKING,
    TAKING_COVER,
    STUNNED,
    DYING
}
