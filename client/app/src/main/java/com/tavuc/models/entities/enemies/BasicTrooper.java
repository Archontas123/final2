package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.managers.WorldManager;
import com.tavuc.utils.Vector2D;
import com.tavuc.models.entities.Entity;

import java.awt.Graphics2D;

/**
 * Basic AI driven trooper used for tests.
 */
public class BasicTrooper extends Trooper implements TargetHolder {

    private final WorldManager world;
    private Entity target;

    public BasicTrooper(double x, double y, WorldManager world, TrooperWeapon weapon, int formationIndex) {
        super(x, y, 20, 20, 2.0, 5, weapon, formationIndex);
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
        suppression.update(0.016);
        if (target != null) {
            Vector2D toTarget = new Vector2D(target.getX() - getX(), target.getY() - getY());
            Vector2D moveDir = pathfinding.getNextMove(new Vector2D(getX(), getY()), new Vector2D(target.getX(), target.getY()));
            Vector2D formationOffset = formation.getSlot(getFormationIndex());
            moveDir.add(formationOffset);

            if (getHealth() < getMaxHealth() / 2) {
                Vector2D cover = coverSystem.findCoverPosition(new Vector2D(getX(), getY()), new Vector2D(target.getX(), target.getY()));
                moveDir = new Vector2D(cover.getX() - getX(), cover.getY() - getY());
                moveDir.normalize();
            }

            setDx(moveDir.getX() * getVelocity());
            setDy(moveDir.getY() * getVelocity());
            move();

            double dist = Math.hypot(toTarget.getX(), toTarget.getY());
            if (dist <= getWeapon().getRange()) {
                combatBehavior.performAttack(target);
                suppression.suppress(this, target);
            } else if (dist <= getWeapon().getRange() * 2) {
                suppression.suppress(this, target);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.fillRect((int)(getX()-offsetX),(int)(getY()-offsetY),20,20);
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
