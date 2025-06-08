package com.tavuc.ai;

import com.tavuc.utils.Vector2D;

/**
 * Basic interface for AI pathfinding.
 */
public interface PathfindingAgent {
    /**
     * Calculates the next movement step for an entity moving from the
     * given start position toward the given target.
     *
     * @param start  current location of the entity
     * @param target desired target location
     * @return direction vector for the next movement step
     */
    Vector2D getNextMove(Vector2D start, Vector2D target);
}
