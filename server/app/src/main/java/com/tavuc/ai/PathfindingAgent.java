package com.tavuc.ai;

/**
 * Basic interface for AI pathfinding on the server.
 */
public interface PathfindingAgent {
    /**
     * Calculates the next movement step for an entity.
     *
     * @param startX starting X position
     * @param startY starting Y position
     * @param targetX target X position
     * @param targetY target Y position
     * @return movement vector of length 2 {dx, dy}
     */
    int[] getNextMove(int startX, int startY, int targetX, int targetY);
}
