package com.tavuc.combat.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

public class MeleeWeapon extends WeaponBase {
    protected float swingArc;
    protected float knockback;

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
