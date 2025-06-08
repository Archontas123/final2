package com.tavuc.ai;

import com.tavuc.managers.WorldManager;
import com.tavuc.models.entities.Entity;
import com.tavuc.models.entities.Player;

import java.util.List;

/**
 * Targets the closest player from the world manager's list.
 */
public class BasicTargetingSystem implements TargetingSystem {
    private final WorldManager world;
    private final Entity self;

    public BasicTargetingSystem(WorldManager world, Entity self) {
        this.world = world;
        this.self = self;
    }

    @Override
    public Entity acquireTarget() {
        List<Player> players = world.getOtherPlayers();
        Entity closest = null;
        double best = Double.MAX_VALUE;
        for (Player p : players) {
            double dx = p.getX() - self.getX();
            double dy = p.getY() - self.getY();
            double dist = dx*dx + dy*dy;
            if (dist < best) { best = dist; closest = p; }
        }
        return closest;
    }
}
