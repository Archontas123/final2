package com.tavuc.ecs.systems;

import com.tavuc.models.entities.Entity;

/**
 * Simple weak point utility used on the server side.
 */
public class WeakPointSystem {
    private static final double BONUS_MULTIPLIER = 1.5;

    public int applyWeakPointDamage(Entity attacker, Entity target, int baseDamage) {
        if (isBehind(attacker, target)) {
            return (int) Math.round(baseDamage * BONUS_MULTIPLIER);
        }
        return baseDamage;
    }

    public boolean isBehind(Entity attacker, Entity target) {
        double targetDir;
        try {
            targetDir = (double)target.getClass().getMethod("getDirection").invoke(target);
        } catch (Exception e) {
            return false;
        }
        double angleToAttacker = Math.atan2(attacker.getY() - target.getY(), attacker.getX() - target.getX());
        double diff = Math.abs(normalize(angleToAttacker - targetDir));
        return Math.abs(Math.PI - diff) < Math.PI / 4;
    }

    private double normalize(double angle) {
        while (angle <= -Math.PI) angle += Math.PI * 2;
        while (angle > Math.PI) angle -= Math.PI * 2;
        return angle;
    }
}
