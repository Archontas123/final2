package com.tavuc.weapons;

/**
 * Determines color and damage bonus of a lightsaber.
 */
public enum LightsaberCrystal {
    BLUE(1.0),
    GREEN(1.1),
    RED(1.2);

    private final double damageMultiplier;

    LightsaberCrystal(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }
}
