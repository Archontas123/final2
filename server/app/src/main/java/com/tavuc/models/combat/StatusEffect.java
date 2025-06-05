package com.tavuc.models.combat;

/**
 * Minimal status effect used by the combat component.
 */
public abstract class StatusEffect {
    protected long startTime = System.currentTimeMillis();
    protected long durationMs;

    public abstract void update();

    public boolean isExpired() {
        return System.currentTimeMillis() - startTime > durationMs;
    }

    public float modifyIncomingDamage(float damage) {
        return damage;
    }
}
