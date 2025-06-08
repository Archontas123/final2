package com.tavuc.ai;

import com.tavuc.models.entities.Entity;

/**
 * Simple combat behavior that damages the target if within range.
 */
public class BasicCombatBehavior implements CombatBehavior {

    private final Entity attacker;
    private final int damage;
    private final double range;

    public BasicCombatBehavior(Entity attacker, int damage, double range) {
        this.attacker = attacker;
        this.damage = damage;
        this.range = range;
    }

    @Override
    public void performAttack(Entity target) {
        if (target == null) return;
        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        if (Math.hypot(dx, dy) <= range) {
            target.takeDamage(damage);
        }
    }
}
