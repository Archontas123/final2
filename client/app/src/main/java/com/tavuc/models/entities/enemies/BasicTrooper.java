package com.tavuc.models.entities.enemies;

import com.tavuc.ai.*;
import com.tavuc.managers.WorldManager;
import com.tavuc.utils.Vector2D;
import com.tavuc.models.entities.Entity;
import java.util.List;

import java.awt.Graphics2D;

/**
 * Basic AI driven trooper used for tests.
 */
public class BasicTrooper extends Trooper implements TargetHolder {

    private final WorldManager world;
    private Entity target;
    private final TacticalCoordinator coordinator = new TacticalCoordinator(List.of());
    private Vector2D flankOffset = new Vector2D();
    private double flankTimer = 0;

    private double distanceToTarget() {
        if (target == null) return Double.MAX_VALUE;
        return Math.hypot(target.getX() - getX(), target.getY() - getY());
    }

    public BasicTrooper(int id, double x, double y, WorldManager world, TrooperWeapon weapon, int formationIndex) {
        super(id, x, y, 20, 20, 2.0, 5, weapon, formationIndex);
        this.world = world;
        this.pathfinding = new BasicPathfindingAgent(world);
        this.targeting = new BasicTargetingSystem(world, this);
        this.combatBehavior = new BasicCombatBehavior(this,1, weapon.getRange());

        // Setup basic state transitions
        stateMachine.addTransition(AIState.SEARCHING, new StateTransition(AIState.PURSUING,
                () -> target != null));
        stateMachine.addTransition(AIState.PURSUING, new StateTransition(AIState.ATTACKING,
                () -> distanceToTarget() <= getWeapon().getRange()));
        stateMachine.addTransition(AIState.PURSUING, new StateTransition(AIState.FLANKING,
                () -> distanceToTarget() <= getWeapon().getRange()*1.5 && flankTimer <= 0));
        stateMachine.addTransition(AIState.PURSUING, new StateTransition(AIState.RETREATING,
                () -> getHealth() < getMaxHealth()*0.3));
        stateMachine.addTransition(AIState.FLANKING, new StateTransition(AIState.ATTACKING,
                () -> distanceToTarget() <= getWeapon().getRange()));
        stateMachine.addTransition(AIState.RETREATING, new StateTransition(AIState.SEARCHING,
                () -> distanceToTarget() > getWeapon().getRange()*3));
        stateMachine.addTransition(AIState.ATTACKING, new StateTransition(AIState.PURSUING,
                () -> distanceToTarget() > getWeapon().getRange()));
        stateMachine.setCurrentState(AIState.SEARCHING);
    }

    @Override
    public void update() {
        if (target == null || !target.isAlive()) {
            target = targeting.acquireTarget();
        }

        suppression.update(0.016);
        stateMachine.update();

        if (target == null) return;

        switch (stateMachine.getCurrentState()) {
            case SEARCHING -> {
                // idle while searching
            }
            case PURSUING -> {
                Vector2D moveDir = pathfinding.getNextMove(new Vector2D(getX(), getY()), new Vector2D(target.getX(), target.getY()));
                Vector2D formationOffset = formation.getSlot(getFormationIndex());
                moveDir.add(formationOffset);
                setDx(moveDir.getX() * getVelocity());
                setDy(moveDir.getY() * getVelocity());
                move();
                flankTimer = Math.max(0, flankTimer - 0.016);
            }
            case FLANKING -> {
                if (flankTimer <= 0) {
                    int[] off = coordinator.getFlankOffset(getFormationIndex());
                    flankOffset.set(off[0], off[1]);
                    flankTimer = 2.0;
                }
                Vector2D pos = new Vector2D(target.getX() + flankOffset.getX(), target.getY() + flankOffset.getY());
                Vector2D dir = pathfinding.getNextMove(new Vector2D(getX(), getY()), pos);
                setDx(dir.getX() * getVelocity());
                setDy(dir.getY() * getVelocity());
                move();
                flankTimer = Math.max(0, flankTimer - 0.016);
            }
            case RETREATING -> {
                Vector2D away = new Vector2D(getX() - target.getX(), getY() - target.getY());
                away.normalize();
                setDx(away.getX() * getVelocity());
                setDy(away.getY() * getVelocity());
                move();
            }
            case ATTACKING -> {
                combatBehavior.performAttack(target);
                suppression.suppress(this, target);
            }
            default -> {}
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
