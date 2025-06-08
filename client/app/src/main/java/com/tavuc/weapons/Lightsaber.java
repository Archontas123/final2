package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Basic implementation of a lightsaber weapon.
 */
public class Lightsaber extends Weapon {
    private LightsaberCrystal crystal;
    private double reach;
    private SwingPattern currentSwing = SwingPattern.SWING_ONE;
    private boolean isBlocking = false;

    public Lightsaber(LightsaberCrystal crystal, double reach, WeaponStats stats) {
        super(WeaponType.LIGHTSABER, stats);
        this.crystal = crystal;
        this.reach = reach;
    }

    @Override
    public void primaryAttack(Player wielder, Vector2D targetPosition) {
        if (!canAttack()) return;
        sounds.play("lightsaber_swing");
        effects.spawn("blade_trail");
        cooldowns.setCooldown("primary", (long) (stats.getCooldown() * 1000));

        // Cycle swing pattern for combo
        switch (currentSwing) {
            case SWING_ONE:
                currentSwing = SwingPattern.SWING_TWO;
                break;
            case SWING_TWO:
                currentSwing = SwingPattern.SWING_THREE;
                break;
            default:
                currentSwing = SwingPattern.SWING_ONE;
                break;
        }
    }

    @Override
    public void secondaryAttack(Player wielder) {
        isBlocking = !isBlocking;
        animations.play(isBlocking ? "block_start" : "block_end");
    }

    @Override
    public boolean canAttack() {
        return !cooldowns.isOnCooldown("primary") && !isBlocking;
    }

    public boolean isBlocking() {
        return isBlocking;
    }
}
