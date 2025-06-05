package com.tavuc.combat.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

public class LightsaberWeapon extends MeleeWeapon {
    private String bladeColor;
    private float bladeLength;
    private boolean dualBladed;

    public LightsaberWeapon(String id, String name, float damage, float range) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.range = range;
        this.attackSpeed = 1.5f;
        this.attackCooldownMs = (long) (1000 / attackSpeed);
        this.swingArc = (float) (Math.PI * 0.6);
        this.bladeColor = "BLUE";
        this.bladeLength = 1.0f;
        this.dualBladed = false;
    }

    @Override
    public boolean performAttack(Player attacker, Vector2D direction) {
        if (!super.performAttack(attacker, direction)) {
            return false;
        }
        return true;
    }
}
