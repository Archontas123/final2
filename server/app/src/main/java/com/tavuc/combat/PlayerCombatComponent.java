package com.tavuc.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.tavuc.combat.weapons.LightsaberWeapon;
import com.tavuc.combat.weapons.WeaponBase;
import com.tavuc.combat.weapons.WeaponRegistry;
import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

public class PlayerCombatComponent {
    private Player player;
    private WeaponBase equippedWeapon;
    private float health;
    private float maxHealth;
    private Map<String, Object> activeEffects = new HashMap<>();
    private boolean attacking;
    private Vector2D attackDirection;
    private long lastAttackTime;
    private long attackAnimationDurationMs;
    private boolean attackProcessed;

    public PlayerCombatComponent(Player player) {
        this.player = player;
        this.health = 100.0f;
        this.maxHealth = 100.0f;
        this.equippedWeapon = WeaponRegistry.createWeaponInstance("lightsaber_1");
    }

    public boolean attemptAttack(Vector2D direction) {
        if (equippedWeapon == null || !equippedWeapon.canAttack() || attacking) {
            return false;
        }
        attacking = true;
        attackDirection = direction;
        lastAttackTime = System.currentTimeMillis();
        attackAnimationDurationMs = 500;
        attackProcessed = false;
        return equippedWeapon.performAttack(player, direction);
    }

    public void update() {
        Iterator<Map.Entry<String, Object>> it = activeEffects.entrySet().iterator();
        while (it.hasNext()) {
            it.next();
        }
        if (attacking && System.currentTimeMillis() - lastAttackTime > attackAnimationDurationMs) {
            attacking = false;
        }
    }

    public void takeDamage(float amount, Player attacker) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public Vector2D getAttackDirection() {
        return attackDirection;
    }

    public boolean wasAttackProcessed() {
        return attackProcessed;
    }

    public void setAttackProcessed(boolean processed) {
        this.attackProcessed = processed;
    }

    public WeaponBase getEquippedWeapon() {
        return equippedWeapon;
    }

    public void equipWeapon(String weaponId) {
        WeaponBase newWeapon = WeaponRegistry.createWeaponInstance(weaponId);
        if (newWeapon != null) {
            this.equippedWeapon = newWeapon;
        }
    }
}
