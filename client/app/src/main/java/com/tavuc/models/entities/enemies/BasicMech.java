package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.managers.WorldManager;
import com.tavuc.utils.Vector2D;
import com.tavuc.models.entities.Entity;

import java.awt.Graphics2D;

/**
 * Simple mech enemy with melee attack.
 */
public class BasicMech extends Mech implements TargetHolder {

    private final WorldManager world;
    private Entity target;
    private int slamCooldown = 0;

    public BasicMech(double x, double y, WorldManager world) {
        super(x, y, 30, 30, 1.5, 10);
        this.world = world;
        this.pathfinding = new BasicPathfindingAgent(world);
        this.targeting = new BasicTargetingSystem(world, this);
        this.combatBehavior = new BasicCombatBehavior(this,2, 25);
    }

    @Override
    public void update() {
        if (target == null || !target.isAlive()) {
            target = targeting.acquireTarget();
        }
        if (target != null) {
            Vector2D toTarget = new Vector2D(target.getX() - getX(), target.getY() - getY());
            setDirection(Math.atan2(toTarget.getY(), toTarget.getX()));
            Vector2D next = pathfinding.getNextMove(new Vector2D(getX(), getY()), new Vector2D(target.getX(), target.getY()));
            setDx(next.getX() * getVelocity());
            setDy(next.getY() * getVelocity());
            move();

            double dist = Math.hypot(toTarget.getX(), toTarget.getY());
            if (dist <= 50 && dist > 25) {
                chargeAttack(target);
            } else if (dist <= 25) {
                comboAttack(target, 3);
                if (slamCooldown <= 0) {
                    slamAOE(java.util.List.of(target), 2);
                    slamCooldown = 60; // cooldown frames
                }
            }
        }
        if (slamCooldown > 0) slamCooldown--;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.fillOval((int)(getX()-offsetX),(int)(getY()-offsetY),30,30);
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
