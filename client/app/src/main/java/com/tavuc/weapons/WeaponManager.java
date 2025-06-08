package com.tavuc.weapons;

/**
 * Handles weapon switching.
 */
public class WeaponManager {
    private Weapon[] equippedWeapons;
    private int currentWeaponIndex;
    private double switchCooldown;
    private SwitchAnimation currentSwitch = SwitchAnimation.DEFAULT;
    private long lastSwitchTime = 0;

    public WeaponManager(Weapon[] weapons, double switchCooldown) {
        this.equippedWeapons = weapons;
        this.switchCooldown = switchCooldown;
        this.currentWeaponIndex = 0;
    }

    public Weapon getCurrentWeapon() {
        return equippedWeapons[currentWeaponIndex];
    }

    public void nextWeapon() {
        if (System.currentTimeMillis() - lastSwitchTime < switchCooldown * 1000) return;
        lastSwitchTime = System.currentTimeMillis();
        currentWeaponIndex = (currentWeaponIndex + 1) % equippedWeapons.length;
    }
}
