package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.models.entities.Entity;

/**
 * Minimal server side trooper for tests.
 */
public class BasicTrooper extends Trooper implements TargetHolder {

    private Entity target;
    private final PathfindingAgent path;
    private final TargetingSystem targetingSys;
    private final CombatBehavior combat;

    public BasicTrooper(int id, String name, int x, int y, TrooperWeapon weapon,
                        boolean[][] blocked, Entity target, int formationIndex) {
        super(id, name, x, y, 5, 20, 20, weapon, formationIndex);
        this.target = target;
        this.path = new BasicPathfindingAgent(blocked);
        this.targetingSys = new BasicTargetingSystem(java.util.List.of(target));
        this.combat = new BasicCombatBehavior(this,1,5);
    }

    @Override
    public void update() {
        if (target == null || target.getHealth() <= 0) {
            target = targetingSys.acquireTarget();
        }
        suppression.update(0.016);
        if (target != null) {
            int[] mv = path.getNextMove(getX(), getY(), target.getX(), target.getY());
            int[] slot = formation.getSlot(getFormationIndex());
            double dx = mv[0] + slot[0];
            double dy = mv[1] + slot[1];

            if (getHealth() < 2.5) {
                int[] cover = coverSystem.findCoverPosition(getX(), getY(), target.getX(), target.getY());
                dx = cover[0] - getX();
                dy = cover[1] - getY();
                double len = Math.hypot(dx, dy);
                if (len != 0) { dx /= len; dy /= len; }
            }

            setDx(dx);
            setDy(dy);
            super.update();

            double dist = Math.hypot(target.getX()-getX(), target.getY()-getY());
            if (dist <= 5) {
                combat.performAttack(target);
                suppression.suppress(this, target);
            } else if (dist <= 10) {
                suppression.suppress(this, target);
            }
        }
    }

    @Override
    public void setTarget(Entity target) {
        this.target = target;
    }

    @Override
    public Entity getTarget() {
        return target;
    }
}
