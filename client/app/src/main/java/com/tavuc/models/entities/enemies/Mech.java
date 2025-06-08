package com.tavuc.models.entities.enemies;

/**
 * Heavy mech enemy.
 */
public abstract class Mech extends Enemy {
    private Object charge;
    private Object combos;
    private Object armor;
    private Object weakPoints;

    public Mech(double x, double y, int width, int height, double velocity, int maxHealth) {
        super(x, y, width, height, velocity, maxHealth, EnemyType.MECH);
    }
}
