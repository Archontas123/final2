package com.tavuc.ai;

/**
 * Placeholder for difficulty scaling settings.
 */
public class DifficultyScaling {
    private double multiplier = 1.0;

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    /** Applies the current multiplier to a base value. */
    public double apply(double value) {
        return value * multiplier;
    }
}
