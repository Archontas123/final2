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
    private boolean parrying;
    private long parryStartTime;
    private long lastParryTime;

    public static final long PARRY_DURATION_MS = 300;
    public static final long PARRY_COOLDOWN_MS = 1000;

    public PlayerCombatComponent(Player player) {
        this.player = player;
        this.equippedWeapon = WeaponRegistry.createWeaponInstance("lightsaber_1");
        this.attackProcessed = false;
        this.parrying = false;
        this.parryStartTime = 0;
        this.lastParryTime = 0;
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

    public boolean attemptParry() {
        long now = System.currentTimeMillis();
        if (parrying) return false;
        if (now - lastParryTime < PARRY_COOLDOWN_MS) return false;
        this.parrying = true;
        this.parryStartTime = now;
        this.lastParryTime = now;
        return true;
    }

    public void update() {
        if (attacking && System.currentTimeMillis() - lastAttackTime > 500) {
            attacking = false;
            attackProcessed = false;
            attackDirection = null;
        }
        if (parrying && System.currentTimeMillis() - parryStartTime > PARRY_DURATION_MS) {
            parrying = false;
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

    public boolean isParrying() {
        if (parrying && System.currentTimeMillis() - parryStartTime > PARRY_DURATION_MS) {
            parrying = false;
        }
        return parrying;
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
