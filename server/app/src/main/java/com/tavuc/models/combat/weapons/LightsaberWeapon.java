package com.tavuc.models.combat.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Simple lightsaber weapon implementation.
 */
public class LightsaberWeapon extends MeleeWeapon {
    private String bladeColor;

    public LightsaberWeapon(String id, String name, float damage, float range) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.range = range;
        this.attackSpeed = 1.5f;
        this.attackCooldownMs = (long) (1000 / attackSpeed);
        this.swingArc = (float) (Math.PI * 0.6);
        this.bladeColor = "BLUE";
    }

    @Override
    public boolean performAttack(Player attacker, Vector2D direction) {
        return super.performAttack(attacker, direction);
    }

    public String getBladeColor() {
        return bladeColor;
    }
}
