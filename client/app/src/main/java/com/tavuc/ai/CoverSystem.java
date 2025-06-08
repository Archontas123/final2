package com.tavuc.ai;

import com.tavuc.utils.Vector2D;

/**
 * Very small helper used by enemies to pick a rough cover position.
 */
public class CoverSystem {

    /**
     * Calculates a simple cover position away from the threat.
     *
     * @param from   current position of the entity
     * @param threat position of the threat
     * @return suggested cover position
     */
    public Vector2D findCoverPosition(Vector2D from, Vector2D threat) {
        Vector2D dir = new Vector2D(from.getX() - threat.getX(), from.getY() - threat.getY());
        if (dir.length() == 0) {
            return new Vector2D(from.getX(), from.getY());
        }
        dir.normalize();
        dir.scale(100); // move some distance away from the threat
        return new Vector2D(from.getX() + dir.getX(), from.getY() + dir.getY());
    }
}
