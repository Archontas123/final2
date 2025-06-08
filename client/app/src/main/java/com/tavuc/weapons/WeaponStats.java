package com.tavuc.weapons;

/**
 * Simple container for weapon statistics.
 */
public class WeaponStats {
    private double damage;
    private double range;
    private double cooldown;

    public WeaponStats(double damage, double range, double cooldown) {
        this.damage = damage;
        this.range = range;
        this.cooldown = cooldown;
    }

    public double getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }

    public double getCooldown() {
        return cooldown;
    }
}
