package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.managers.WorldManager;
import com.tavuc.utils.Vector2D;
import com.tavuc.models.entities.Entity;

import java.awt.Graphics2D;

/**
 * Basic AI driven trooper used for tests.
 */
public class BasicTrooper extends Trooper {

    private final WorldManager world;
    private Entity target;

    public BasicTrooper(double x, double y, WorldManager world, TrooperWeapon weapon) {
        super(x, y, 20, 20, 2.0, 5, weapon);
        this.world = world;
        this.pathfinding = new BasicPathfindingAgent(world);
        this.targeting = new BasicTargetingSystem(world, this);
        this.combatBehavior = new BasicCombatBehavior(this,1, weapon.getRange());
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
            if (Math.hypot(target.getX()-getX(), target.getY()-getY()) <= getWeapon().getRange()) {
                combatBehavior.performAttack(target);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.fillRect((int)(getX()-offsetX),(int)(getY()-offsetY),20,20);
    }
}
