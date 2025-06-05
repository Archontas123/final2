package com.tavuc.models.combat.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Base class for all weapons.
 */
public abstract class WeaponBase implements Cloneable {
    protected String id;
    protected String name;
    protected float damage;
    protected float range;
    protected float attackSpeed;
    protected long lastAttackTime;
    protected long attackCooldownMs;

    /**
     * Perform an attack with this weapon.
     *
     * @param attacker  player using the weapon
     * @param direction direction of the attack
     * @return true if attack should be processed
     */
    public abstract boolean performAttack(Player attacker, Vector2D direction);

    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldownMs;
    }

    public float getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public WeaponBase clone() throws CloneNotSupportedException {
        return (WeaponBase) super.clone();
    }
}
