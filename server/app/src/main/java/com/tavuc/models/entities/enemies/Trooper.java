package com.tavuc.models.entities.enemies;

/**
 * Basic trooper enemy implementation for the server.
 */
public abstract class Trooper extends Enemy {
    private TrooperWeapon weapon;
    private Object coverSystem;
    private Object formation;
    private Object suppression;

    public Trooper(int id, String name, int x, int y, double health, int width, int height, TrooperWeapon weapon) {
        super(id, name, x, y, health, width, height, EnemyType.TROOPER);
        this.weapon = weapon;
    }

    public TrooperWeapon getWeapon() {
        return weapon;
    }
}
