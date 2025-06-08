package com.tavuc.models.entities.enemies;

/**
 * Heavy mech enemy for the server.
 */
public abstract class Mech extends Enemy {
    private Object charge;
    private Object combos;
    private Object armor;
    private Object weakPoints;

    public Mech(int id, String name, int x, int y, double health, int width, int height) {
        super(id, name, x, y, health, width, height, EnemyType.MECH);
    }
}
