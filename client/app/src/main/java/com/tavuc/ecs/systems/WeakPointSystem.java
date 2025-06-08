package com.tavuc.ecs.systems;

import com.tavuc.models.entities.Entity;

/**
 * Utility system that determines if an attack strikes a target's weak point.
 * Attacks from behind the target will deal bonus damage.
 */
public class WeakPointSystem {
    /** Multiplier applied when hitting a weak point. */
    private static final double BONUS_MULTIPLIER = 1.5;

    /**
     * Calculates bonus damage if the attacker is behind the target.
     * @param attacker entity performing the attack
     * @param target   entity being hit
     * @param baseDamage base damage amount
     * @return modified damage after applying bonus for a rear attack
     */
    public int applyWeakPointDamage(Entity attacker, Entity target, int baseDamage) {
        if (isBehind(attacker, target)) {
            return (int) Math.round(baseDamage * BONUS_MULTIPLIER);
        }
        return baseDamage;
    }

    /**
     * Determines if attacker is behind the target based on the target's facing direction.
     */
    public boolean isBehind(Entity attacker, Entity target) {
        double targetDir;
        try {
            targetDir = (double)target.getClass().getMethod("getDirection").invoke(target);
        } catch (Exception e) {
            return false;
        }
        double angleToAttacker = Math.atan2(attacker.getY() - target.getY(), attacker.getX() - target.getX());
        double diff = Math.abs(normalize(angleToAttacker - targetDir));
        return Math.abs(Math.PI - diff) < Math.PI / 4; // 45 deg tolerance
    }

    private double normalize(double angle) {
        while (angle <= -Math.PI) angle += Math.PI * 2;
        while (angle > Math.PI) angle -= Math.PI * 2;
        return angle;
    }
}
