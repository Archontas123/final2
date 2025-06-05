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
    private boolean attackProcessed;
    private Vector2D attackDirection;
    private long lastAttackTime;

    public PlayerCombatComponent(Player player) {
        this.player = player;
        this.equippedWeapon = WeaponRegistry.createWeaponInstance("lightsaber_1");
        this.attackProcessed = false;
    }

    public boolean attemptAttack(Vector2D direction) {
        if (equippedWeapon == null) return false;
        if (!equippedWeapon.canAttack()) return false;
        this.attacking = true;
        this.attackDirection = direction;
        this.attackProcessed = false;
        this.lastAttackTime = System.currentTimeMillis();
        return equippedWeapon.performAttack(player, direction);
    }

    public void update() {
        if (attacking && System.currentTimeMillis() - lastAttackTime > 500) {
            attacking = false;
            attackProcessed = false;
            attackDirection = null;
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

    public boolean wasAttackProcessed() {
        return attackProcessed;
    }

    public void setAttackProcessed(boolean processed) {
        this.attackProcessed = processed;
    }

    public void takeDamage(float amount, Player attacker) {
        float finalDamage = amount;
        for (StatusEffect effect : activeEffects.values()) {
            finalDamage = effect.modifyIncomingDamage(finalDamage);
        }
        this.health -= finalDamage;
        if (this.health < 0) {
            this.health = 0;
        }
    }
}
