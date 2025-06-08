package com.tavuc.models.entities.enemies;

/**
 * Heavy mech enemy.
 */
import com.tavuc.models.entities.Entity;
import com.tavuc.utils.Vector2D;
import java.util.List;

/**
 * Heavy mech enemy.
 */
public abstract class Mech extends Enemy {
    private boolean charging;
    private int comboHits;
    private double slamRadius = 30;

    public Mech(double x, double y, int width, int height, double velocity, int maxHealth) {
        super(x, y, width, height, velocity, maxHealth, EnemyType.MECH);
    }

    /** Performs a quick charge toward the target and attempts an attack. */
    protected void chargeAttack(Entity target) {
        if (target == null) return;
        Vector2D dir = new Vector2D(target.getX() - getX(), target.getY() - getY());
        if (dir.length() == 0) return;
        dir.normalize();
        setDx(dir.getX() * getVelocity() * 3);
        setDy(dir.getY() * getVelocity() * 3);
        move();
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
