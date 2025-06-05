package com.tavuc.models.combat.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Base class for melee weapons.
 */
public class MeleeWeapon extends WeaponBase {
    protected float swingArc;

    @Override
    public boolean performAttack(Player attacker, Vector2D direction) {
        if (!canAttack()) {
            return false;
        }
        lastAttackTime = System.currentTimeMillis();
        return true;
    }

    public float getSwingArc() {
        return swingArc;
    }
}
