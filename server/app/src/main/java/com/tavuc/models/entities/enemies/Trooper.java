package com.tavuc.models.entities.enemies;

/**
 * Basic trooper enemy implementation for the server.
 */
import com.tavuc.ai.CoverSystem;
import com.tavuc.ai.FormationController;
import com.tavuc.ai.SuppressionBehavior;

public abstract class Trooper extends Enemy {
    private final TrooperWeapon weapon;
    protected final CoverSystem coverSystem;
    protected final FormationController formation;
    protected final SuppressionBehavior suppression;
    protected int formationIndex;

    public Trooper(int id, String name, int x, int y, double health, int width, int height,
                   TrooperWeapon weapon, int formationIndex) {
        super(id, name, x, y, health, width, height, EnemyType.TROOPER);
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
