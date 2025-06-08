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
    private boolean ads = false;

    // Simple recoil pattern (in degrees) that cycles with each shot
    private static final double[] RECOIL_PATTERN = {0.0, 0.5, -0.3, 0.7, -0.5};
    private int recoilIndex = 0;

    private boolean charging = false;
    private long chargeStart = 0;

    public BlasterPistol(int magazineSize, double reloadTime, double accuracy, WeaponStats stats) {
        super(WeaponType.BLASTER_PISTOL, stats);
        this.magazineSize = magazineSize;
        this.currentAmmo = magazineSize;
        this.reloadTime = reloadTime;
        this.accuracy = accuracy;
    }

    /**
     * Enables or disables aiming down sights (ADS) mode.
     */
    public void setADS(boolean ads) {
        this.ads = ads;
    }

    public boolean isADS() {
        return ads;
    }

    @Override
    public void primaryAttack(Player wielder, Vector2D targetPosition) {
        if (!canAttack()) return;
        if (currentAmmo <= 0) {
            secondaryAttack(wielder);
            return;
        }
        currentAmmo--;

        // Calculate base direction towards the target
        Vector2D direction = new Vector2D(
                targetPosition.getX() - wielder.getX(),
                targetPosition.getY() - wielder.getY());
        direction.normalize();

        // Apply accuracy modifier based on ADS state
        double finalAccuracy = accuracy;
        if (ads) {
            finalAccuracy = Math.min(1.0, accuracy * 1.5);
        }

        double spread = (1.0 - finalAccuracy) * (Math.random() - 0.5) * 2.0;

        // Apply recoil pattern
        double recoil = Math.toRadians(RECOIL_PATTERN[recoilIndex]);
        recoilIndex = (recoilIndex + 1) % RECOIL_PATTERN.length;

        double angle = Math.atan2(direction.getY(), direction.getX());
        angle += spread + recoil;

        double dx = Math.cos(angle);
        double dy = Math.sin(angle);

        // Normally a projectile would be spawned here. For this mock
        // implementation we just log the shot direction.
        System.out.println("[BlasterPistol] Shot fired with direction: " + dx + "," + dy);

        projectileType = ProjectileType.STANDARD;

        sounds.play("blaster_fire");
        effects.spawn("muzzle_flash", wielder.getX() + wielder.getWidth() / 2.0,
                      wielder.getY() + wielder.getHeight() / 2.0);
        cooldowns.setCooldown("primary", (long) (stats.getCooldown() * 1000));
    }

    /**
     * Performs a charged shot. Holding fire increases damage up to a cap and
     * consumes multiple ammo when released.
     *
     * @param wielder        the player firing the weapon
     * @param targetPosition the aim position
     * @param holdTimeMs     how long the fire button was held in milliseconds
     */
    public void chargeShot(Player wielder, Vector2D targetPosition, long holdTimeMs) {
        if (!canAttack()) return;

        int ammoRequired = 3;
        if (currentAmmo < ammoRequired) {
            secondaryAttack(wielder);
            return;
        }
        currentAmmo -= ammoRequired;

        double chargeRatio = Math.min(1.0, holdTimeMs / 1000.0);
        double damageMultiplier = 1.0 + chargeRatio; // up to 2x damage

        projectileType = ProjectileType.CHARGED;
        System.out.println("[BlasterPistol] Charged shot fired, damage x" + damageMultiplier);

        // Reuse primaryAttack logic for direction and recoil but with charged effects
        sounds.play("blaster_charge_fire");
        effects.spawn("charged_muzzle_flash", wielder.getX() + wielder.getWidth() / 2.0,
                      wielder.getY() + wielder.getHeight() / 2.0);
        cooldowns.setCooldown("primary", (long) (stats.getCooldown() * 1000 * 1.5));

        projectileType = ProjectileType.STANDARD;
    }

    /** Starts tracking a charge shot. */
    public void startCharge() {
        if (!charging) {
            charging = true;
            chargeStart = System.currentTimeMillis();
        }
    }

    /** Releases the charge and fires a charged shot if applicable. */
    public void releaseCharge(Player wielder, Vector2D targetPosition) {
        if (charging) {
            long hold = System.currentTimeMillis() - chargeStart;
            charging = false;
            chargeShot(wielder, targetPosition, hold);
        }
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
