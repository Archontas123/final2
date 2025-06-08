package com.tavuc.models.entities.enemies;

import com.tavuc.ai.AIStateMachine;
import com.tavuc.ai.CombatBehavior;
import com.tavuc.ai.PathfindingAgent;
import com.tavuc.ai.TargetingSystem;
import com.tavuc.ai.CoverSystem;
import com.tavuc.ai.FormationController;
import com.tavuc.ai.SuppressionBehavior;

/**
 * Basic trooper enemy implementation.
 */
public abstract class Trooper extends Enemy {
    private final TrooperWeapon weapon;
    protected final CoverSystem coverSystem;
    protected final FormationController formation;
    protected final SuppressionBehavior suppression;
    protected int formationIndex;

    public Trooper(int id, double x, double y, int width, int height, double velocity, int maxHealth,
                   TrooperWeapon weapon, int formationIndex) {
        super(id, x, y, width, height, velocity, maxHealth, EnemyType.TROOPER);
        this.weapon = weapon;
        this.coverSystem = new CoverSystem();
        this.formation = new FormationController(4);
        this.suppression = new SuppressionBehavior();
        this.formationIndex = formationIndex;
    }

    public TrooperWeapon getWeapon() {
        return weapon;
    }

    public int getFormationIndex() {
        return formationIndex;
    }
}
