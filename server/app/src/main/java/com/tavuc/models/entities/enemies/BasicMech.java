package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.models.entities.Entity;

/**
 * Minimal server side mech.
 */
public class BasicMech extends Mech implements TargetHolder {

    private Entity target;
    private final PathfindingAgent path;
    private final TargetingSystem targetingSys;
    private final CombatBehavior combat;
    private int slamCooldown = 0;

    public BasicMech(int id, String name, int x, int y, boolean[][] blocked, Entity target) {
        super(id, name, x, y, 10, 30, 30);
        this.target = target;
        this.path = new BasicPathfindingAgent(blocked);
        this.targetingSys = new BasicTargetingSystem(java.util.List.of(target));
        this.combat = new BasicCombatBehavior(this,2,25);
    }

    @Override
    public void update() {
        if (target == null || target.getHealth() <= 0) {
            target = targetingSys.acquireTarget();
        }
        if (target != null) {
            int[] mv = path.getNextMove(getX(), getY(), target.getX(), target.getY());
            setDx(mv[0]);
            setDy(mv[1]);
            super.update();

            double dx = target.getX() - getX();
            double dy = target.getY() - getY();
            setDirection(Math.atan2(dy, dx));
            double dist = Math.hypot(dx, dy);
            if (dist <= 50 && dist > 25) {
                chargeAttack(target);
            } else if (dist <= 25) {
                comboAttack(target, 3);
                if (slamCooldown <= 0) {
                    slamAOE(java.util.List.of(target), 2);
                    slamCooldown = 60;
                }
            }
        }
        if (slamCooldown > 0) slamCooldown--;
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
