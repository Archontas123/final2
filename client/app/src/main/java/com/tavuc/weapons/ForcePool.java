package com.tavuc.weapons;

/**
 * Tracks available force energy.
 */
public class ForcePool {
    private double maxEnergy;
    private double currentEnergy;

    public ForcePool(double maxEnergy) {
        this.maxEnergy = maxEnergy;
        this.currentEnergy = maxEnergy;
    }

    public boolean consume(double amount) {
        if (currentEnergy < amount) return false;
        currentEnergy -= amount;
        return true;
    }

    public void regenerate(double amount) {
        currentEnergy = Math.min(maxEnergy, currentEnergy + amount);
    }
}
