package com.tavuc.weapons;

import java.util.EnumMap;
import java.util.Map;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Basic implementation of force powers.
 */
public class ForcePowers extends Weapon {
    private ForcePool forceEnergy;
    private Map<ForceAbility, Cooldown> abilityCooldowns = new EnumMap<>(ForceAbility.class);
    private ForceAlignment alignment;

    public ForcePowers(double maxEnergy, ForceAlignment alignment, WeaponStats stats) {
        super(WeaponType.FORCE_POWERS, stats);
        this.forceEnergy = new ForcePool(maxEnergy);
        this.alignment = alignment;
        for (ForceAbility ability : ForceAbility.values()) {
            abilityCooldowns.put(ability, new Cooldown());
        }
    }

    @Override
    public void primaryAttack(Player wielder, Vector2D targetPosition) {
        useAbility(ForceAbility.FORCE_PUSH);
    }

    @Override
    public void secondaryAttack(Player wielder) {
        useAbility(ForceAbility.FORCE_SLAM);
    }

    private void useAbility(ForceAbility ability) {
        Cooldown cd = abilityCooldowns.get(ability);
        if (cd.isActive()) return;
        // costs are simplified
        if (!forceEnergy.consume(10)) return;
        sounds.play("force_use");
        effects.spawn("force_effect");
        cd.start((long) (stats.getCooldown() * 1000));
    }

    @Override
    public boolean canAttack() {
        return true;
    }
}
