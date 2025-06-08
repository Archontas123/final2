package com.tavuc.models.entities.enemies;

/**
 * Heavy mech enemy for the server.
 */
import com.tavuc.models.entities.Entity;
import java.util.List;

/**
 * Heavy mech enemy for the server.
 */
public abstract class Mech extends Enemy {
    private boolean charging;
    private int comboHits;
    private double slamRadius = 30;

    public Mech(int id, String name, int x, int y, double health, int width, int height) {
        super(id, name, x, y, health, width, height, EnemyType.MECH);
    }

    /** Performs a quick charge toward the target and attempts an attack. */
    protected void chargeAttack(Entity target) {
        if (target == null) return;
        double dx = target.getX() - getX();
        double dy = target.getY() - getY();
        double dist = Math.hypot(dx, dy);
        if (dist == 0) return;
        setDx((dx / dist) * 3);
        setDy((dy / dist) * 3);
        update();
        combatBehavior.performAttack(target);
    }

    /** Performs multiple rapid hits on the target. */
    protected void comboAttack(Entity target, int hits) {
        if (target == null) return;
        for (int i = 0; i < hits; i++) {
            combatBehavior.performAttack(target);
        }
    }

    /** Deals area damage around the mech. */
    protected void slamAOE(List<Entity> targets, int damage) {
        for (Entity e : targets) {
            double dx = e.getX() - getX();
            double dy = e.getY() - getY();
            if (Math.hypot(dx, dy) <= slamRadius) {
                e.takeDamage(damage);
            }
        }
    }
}
