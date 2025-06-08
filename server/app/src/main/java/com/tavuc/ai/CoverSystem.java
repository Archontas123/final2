package com.tavuc.ai;

/**
 * Simple helper for picking cover positions on the server.
 */
public class CoverSystem {

    /**
     * Returns a rough cover position away from the threat.
     */
    public int[] findCoverPosition(int fromX, int fromY, int threatX, int threatY) {
        int dx = fromX - threatX;
        int dy = fromY - threatY;
        double len = Math.hypot(dx, dy);
        if (len == 0) {
            return new int[]{fromX, fromY};
        }
        dx = (int) (dx / len * 100);
        dy = (int) (dy / len * 100);
        return new int[]{fromX + dx, fromY + dy};
    }
}
