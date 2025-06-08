package com.tavuc.models.entities.enemies;

import com.tavuc.ai.AIStateMachine;
import com.tavuc.ai.CombatBehavior;
import com.tavuc.ai.PathfindingAgent;
import com.tavuc.ai.TargetingSystem;

/**
 * Basic trooper enemy implementation.
 */
public abstract class Trooper extends Enemy {
    private TrooperWeapon weapon;
    private Object coverSystem; // placeholder for a cover system
    private Object formation;   // placeholder for formation controller
    private Object suppression; // placeholder for suppression behavior

    public Trooper(double x, double y, int width, int height, double velocity, int maxHealth, TrooperWeapon weapon) {
        super(x, y, width, height, velocity, maxHealth, EnemyType.TROOPER);
        this.weapon = weapon;
    }

    public TrooperWeapon getWeapon() {
        return weapon;
    }
}
