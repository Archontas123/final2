package com.tavuc.models.entities.enemies;

import com.tavuc.ai.AIStateMachine;
import com.tavuc.ai.PathfindingAgent;
import com.tavuc.ai.TargetingSystem;
import com.tavuc.ai.CombatBehavior;
import com.tavuc.ai.DifficultyScaling;
import com.tavuc.models.entities.Entity;

/**
 * Base class for all enemy entities.
 */
public abstract class Enemy extends Entity {
    protected AIStateMachine stateMachine;
    protected PathfindingAgent pathfinding;
    protected TargetingSystem targeting;
    protected CombatBehavior combatBehavior;
    protected EnemyType type;
    protected DifficultyScaling scaling;

    public Enemy(double x, double y, int width, int height, double velocity, int maxHealth, EnemyType type) {
        super(x, y, width, height, velocity, maxHealth);
        this.type = type;
        this.stateMachine = new AIStateMachine();
        this.scaling = new DifficultyScaling();
    }

    public AIStateMachine getStateMachine() {
        return stateMachine;
    }
}
