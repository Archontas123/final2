package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.models.entities.Entity;

/**
 * Minimal server side trooper for tests.
 */
public class BasicTrooper extends Trooper {

    private Entity target;
    private final PathfindingAgent path;
    private final TargetingSystem targetingSys;
    private final CombatBehavior combat;

    public BasicTrooper(int id, String name, int x, int y, TrooperWeapon weapon, boolean[][] blocked, Entity target) {
        super(id, name, x, y, 5, 20, 20, weapon);
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
        if (target != null) {
            int[] mv = path.getNextMove(getX(), getY(), target.getX(), target.getY());
            setDx(mv[0]);
            setDy(mv[1]);
            super.update();
            if (Math.hypot(target.getX()-getX(), target.getY()-getY()) <= 5) {
                combat.performAttack(target);
            }
        }
    }
}
