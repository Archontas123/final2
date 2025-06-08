package com.tavuc.weapons;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple cooldown tracker for weapon actions.
 */
public class CooldownManager {
    private final Map<String, Long> cooldowns = new HashMap<>();

    public void setCooldown(String key, long durationMs) {
        cooldowns.put(key, System.currentTimeMillis() + durationMs);
    }

    public boolean isOnCooldown(String key) {
        Long end = cooldowns.get(key);
        return end != null && System.currentTimeMillis() < end;
    }
}
