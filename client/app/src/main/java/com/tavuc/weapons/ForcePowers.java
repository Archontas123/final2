package com.tavuc.weapons;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;
import com.tavuc.Client;

/**
 * Basic implementation of force powers.
 */
public class ForcePowers extends Weapon {
    private ForcePool forceEnergy;
    private Map<ForceAbility, Cooldown> abilityCooldowns = new EnumMap<>(ForceAbility.class);
    private ForceAlignment alignment;
    private List<Player> validTargets = new ArrayList<>();
    private Player currentTarget;

    public ForcePowers(double maxEnergy, ForceAlignment alignment, WeaponStats stats) {
        super(WeaponType.FORCE_POWERS, stats);
        this.forceEnergy = new ForcePool(maxEnergy);
        this.alignment = alignment;
        for (ForceAbility ability : ForceAbility.values()) {
            abilityCooldowns.put(ability, new Cooldown());
        }
    }

    /** Update list of valid targets within range of the wielder. */
    private void updateTargets(Player wielder) {
        validTargets.clear();
        if (Client.worldManager == null || wielder == null) return;
        for (Player p : Client.worldManager.getOtherPlayers()) {
            double dx = p.getX() - wielder.getX();
            double dy = p.getY() - wielder.getY();
            if (Math.hypot(dx, dy) <= stats.getRange()) {
                validTargets.add(p);
            }
        }
        if (!validTargets.contains(currentTarget)) {
            currentTarget = validTargets.isEmpty() ? null : validTargets.get(0);
        }
    }

    /** Cycle to the next target in range. */
    public void cycleTarget(Player wielder) {
        updateTargets(wielder);
        if (validTargets.isEmpty()) return;
        if (currentTarget == null) {
            currentTarget = validTargets.get(0);
            return;
        }
        int idx = validTargets.indexOf(currentTarget);
        idx = (idx + 1) % validTargets.size();
        currentTarget = validTargets.get(idx);
    }

    public Player getCurrentTarget() {
        return currentTarget;
    }

    @Override
    public void primaryAttack(Player wielder, Vector2D targetPosition) {
        useAbility(ForceAbility.FORCE_PUSH, wielder);
    }

    @Override
    public void secondaryAttack(Player wielder) {
        useAbility(ForceAbility.FORCE_SLAM, wielder);
    }

    /** Perform the Force Choke ability on the current target. */
    public void choke(Player wielder) {
        useAbility(ForceAbility.FORCE_CHOKE, wielder);
    }

    private void useAbility(ForceAbility ability, Player wielder) {
        Cooldown cd = abilityCooldowns.get(ability);
        if (cd.isActive()) return;
        if (!forceEnergy.consume(10)) return;

        updateTargets(wielder);

        switch (ability) {
            case FORCE_CHOKE -> {
                if (currentTarget == null) return;
                double dx = currentTarget.getX() - wielder.getX();
                double dy = currentTarget.getY() - wielder.getY();
                if (Math.hypot(dx, dy) > stats.getRange()) return;
                Client.sendForceAbility(wielder.getPlayerId(), currentTarget.getPlayerId(), ability.name());
            }
            case FORCE_PUSH -> {
                if (currentTarget == null) return;
                double dx = currentTarget.getX() - wielder.getX();
                double dy = currentTarget.getY() - wielder.getY();
                if (Math.hypot(dx, dy) > stats.getRange()) return;
                Client.sendForceAbility(wielder.getPlayerId(), currentTarget.getPlayerId(), ability.name());
            }
            case FORCE_SLAM -> {
                // AoE ability does not require a specific target
                Client.sendForceAbility(wielder.getPlayerId(), -1, ability.name());
            }
        }

        sounds.play("force_use");
        effects.spawn("force_effect");
        cd.start((long) (stats.getCooldown() * 1000));
    }

    @Override
    public boolean canAttack() {
        return true;
    }
}
