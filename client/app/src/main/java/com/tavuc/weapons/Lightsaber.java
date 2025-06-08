package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;
import com.tavuc.Client;
import com.tavuc.ecs.systems.ShipCombatSystem;
import com.tavuc.models.space.Projectile;
import java.util.List;

/**
 * Basic implementation of a lightsaber weapon.
 */
public class Lightsaber extends Weapon {
    private LightsaberCrystal crystal;
    private double reach;
    private SwingPattern currentSwing = SwingPattern.SWING_ONE;
    private boolean isBlocking = false;
    private boolean isCharging = false;
    private long chargeStart = 0;
    private static final long MAX_CHARGE_MS = 1000;

    public Lightsaber(LightsaberCrystal crystal, double reach, WeaponStats stats) {
        super(WeaponType.LIGHTSABER, stats);
        this.crystal = crystal;
        this.reach = reach;
    }

    @Override
    public void primaryAttack(Player wielder, Vector2D targetPosition) {
        if (!canAttack() && !isCharging) return;

        if (!isCharging) {
            isCharging = true;
            chargeStart = System.currentTimeMillis();
            animations.play("charge_start");
            return;
        }

        long chargeTime = System.currentTimeMillis() - chargeStart;
        double chargeRatio = Math.min(1.0, chargeTime / (double) MAX_CHARGE_MS);

        if (targetPosition != null) {
            Vector2D direction = new Vector2D(targetPosition.getX() - wielder.getX(),
                                              targetPosition.getY() - wielder.getY());
            if (direction.length() > 0) {
                direction.normalize();
                direction.scale(reach * (1 + chargeRatio));
                wielder.setX(wielder.getX() + direction.getX());
                wielder.setY(wielder.getY() + direction.getY());
            }
        }

        sounds.play("lightsaber_swing");
        effects.spawn("blade_trail");
        cooldowns.setCooldown("primary", (long) (stats.getCooldown() * 1000));
        cycleSwing();
        isCharging = false;
    }

    private void cycleSwing() {
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
        if (isBlocking) {
            reflectProjectiles(wielder);
        }
    }

    @Override
    public boolean canAttack() {
        return !cooldowns.isOnCooldown("primary") && !isBlocking;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    private void reflectProjectiles(Player wielder) {
        if (Client.currentSpacePanel == null) return;
        try {
            java.lang.reflect.Field f = Client.currentSpacePanel.getClass().getDeclaredField("combatSystem");
            f.setAccessible(true);
            ShipCombatSystem cs = (ShipCombatSystem) f.get(Client.currentSpacePanel);
            if (cs == null) return;
            List<Projectile> projectiles = cs.getProjectiles();
            for (Projectile p : projectiles) {
                double dx = p.getX() - wielder.getX();
                double dy = p.getY() - wielder.getY();
                if (Math.sqrt(dx * dx + dy * dy) <= reach) {
                    p.setVelocityX(-p.getVelocityX());
                    p.setVelocityY(-p.getVelocityY());
                }
            }
        } catch (Exception ignored) {}
    }
}
