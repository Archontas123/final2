package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Simple blaster pistol implementation.
 */
public class BlasterPistol extends Weapon {
    private int magazineSize;
    private int currentAmmo;
    private double reloadTime;
    private ProjectileType projectileType = ProjectileType.STANDARD;
    private double accuracy;
    private long lastReloadStart = 0;

    public BlasterPistol(int magazineSize, double reloadTime, double accuracy, WeaponStats stats) {
        super(WeaponType.BLASTER_PISTOL, stats);
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.reloadTime = reloadTime;
        this.accuracy = accuracy;
    }

    @Override
    public void primaryAttack(Player wielder, Vector2D targetPosition) {
        if (!canAttack()) return;
        if (currentAmmo <= 0) {
            secondaryAttack(wielder);
            return;
        }
        currentAmmo--;
        sounds.play("blaster_fire");
        effects.spawn("muzzle_flash");
        cooldowns.setCooldown("primary", (long) (stats.getCooldown() * 1000));
    }

    @Override
    public void secondaryAttack(Player wielder) {
        if (currentAmmo == magazineSize) return;
        if (System.currentTimeMillis() - lastReloadStart < reloadTime * 1000) return;
        lastReloadStart = System.currentTimeMillis();
        currentAmmo = magazineSize;
        sounds.play("blaster_reload");
    }

    @Override
    public boolean canAttack() {
        return !cooldowns.isOnCooldown("primary") &&
               System.currentTimeMillis() - lastReloadStart >= reloadTime * 1000;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }
}
