package com.tavuc.models.entities.enemies;

import com.tavuc.weapons.ProjectileType;

/**
 * Weapon types for troopers.
 */
public enum TrooperWeapon {
    BLASTER(300, 2.0, ProjectileType.ENERGY),
    LAUNCHER(450, 4.0, ProjectileType.EXPLOSIVE),
    RIFLE(500, 1.5, ProjectileType.KINETIC);

    private final int range;
    private final double cooldown;
    private final ProjectileType projectileType;

    TrooperWeapon(int range, double cooldown, ProjectileType type) {
        this.range = range;
        this.cooldown = cooldown;
        this.projectileType = type;
    }

    public int getRange() {
        return range;
    }

    public double getCooldown() {
        return cooldown;
    }

    public ProjectileType getProjectileType() {
        return projectileType;
    }
}
