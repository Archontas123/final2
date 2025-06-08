package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.managers.WorldManager;
import com.tavuc.utils.Vector2D;
import com.tavuc.models.entities.Entity;

import java.awt.Graphics2D;

/**
 * Simple mech enemy with melee attack.
 */
public class BasicMech extends Mech {

    private final WorldManager world;
    private Entity target;

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
            Vector2D next = pathfinding.getNextMove(new Vector2D(getX(), getY()), new Vector2D(target.getX(), target.getY()));
            setDx(next.getX() * getVelocity());
            setDy(next.getY() * getVelocity());
            move();
            if (Math.hypot(target.getX()-getX(), target.getY()-getY()) <= 25) {
                combatBehavior.performAttack(target);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.fillOval((int)(getX()-offsetX),(int)(getY()-offsetY),30,30);
    }
}
