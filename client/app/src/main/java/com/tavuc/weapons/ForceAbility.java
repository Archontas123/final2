package com.tavuc.weapons;

/**
 * Various force abilities.
 */
public enum ForceAbility {
    FORCE_SLAM(1),
    FORCE_PUSH(2),
    FORCE_CHOKE(3);

    private final int slot;

    ForceAbility(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
