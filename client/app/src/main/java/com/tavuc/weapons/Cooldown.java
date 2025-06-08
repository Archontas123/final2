package com.tavuc.weapons;

/**
 * Represents a single ability cooldown.
 */
public class Cooldown {
    private long endTime = 0;

    public void start(long durationMs) {
        endTime = System.currentTimeMillis() + durationMs;
    }

    public boolean isActive() {
        return System.currentTimeMillis() < endTime;
    }
}
