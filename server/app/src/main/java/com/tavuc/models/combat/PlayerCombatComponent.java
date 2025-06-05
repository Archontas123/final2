package com.tavuc.models.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.tavuc.models.combat.weapons.WeaponBase;
import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Simple combat component attached to players.
 */
public class PlayerCombatComponent {
    private final Player player;
    private WeaponBase equippedWeapon;
    private float health = 100f;
    private final Map<String, StatusEffect> activeEffects = new HashMap<>();
    private boolean attacking;
    private Vector2D attackDirection;
    private long lastAttackTime;

    public PlayerCombatComponent(Player player) {
        this.player = player;
        this.equippedWeapon = WeaponRegistry.createWeaponInstance("lightsaber_1");
    }

    public boolean attemptAttack(Vector2D direction) {
        if (equippedWeapon == null) return false;
        if (!equippedWeapon.canAttack()) return false;
        this.attacking = true;
        this.attackDirection = direction;
        this.lastAttackTime = System.currentTimeMillis();
        return equippedWeapon.performAttack(player, direction);
    }

    public void update() {
        if (attacking && System.currentTimeMillis() - lastAttackTime > 500) {
            attacking = false;
        }
        Iterator<Map.Entry<String, StatusEffect>> it = activeEffects.entrySet().iterator();
        while (it.hasNext()) {
            StatusEffect effect = it.next().getValue();
            effect.update();
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }

    public float getHealth() {
        return health;
    }

    public WeaponBase getEquippedWeapon() {
        return equippedWeapon;
    }

    public Vector2D getAttackDirection() {
        return attackDirection;
    }

    public boolean isAttacking() {
        return attacking;
    }
}
