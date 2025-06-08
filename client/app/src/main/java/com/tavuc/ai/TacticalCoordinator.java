package com.tavuc.ai;

import com.tavuc.models.entities.Entity;
import com.tavuc.models.entities.enemies.Enemy;

import java.util.List;

/**
 * Coordinates a squad of enemies so they can share targets and flank.
 */
public class TacticalCoordinator {
    private final List<? extends Enemy> squad;

    public TacticalCoordinator(List<? extends Enemy> squad) {
        this.squad = squad;
    }

    /** Assigns a common target to all squad members that support it. */
    public void shareTarget(Entity target) {
        for (Enemy e : squad) {
            if (e instanceof TargetHolder th) {
                th.setTarget(target);
            }
        }
    }

    /**
     * Calculates a flank offset around a target for the given index.
     * Offsets are spaced in 45 degree increments at a fixed distance.
     */
    public int[] getFlankOffset(int index) {
        double angle = Math.toRadians(90 + index * 45);
        int dist = 30;
        int x = (int) (Math.cos(angle) * dist);
        int y = (int) (Math.sin(angle) * dist);
        return new int[] { x, y };
    }
}
