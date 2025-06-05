package com.tavuc.combat.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

public abstract class WeaponBase implements Cloneable {
    protected String id;
    protected String name;
    protected float damage;
    protected float range;
    protected float attackSpeed;
    protected float weight;
    protected long lastAttackTime;
    protected long attackCooldownMs;

    public abstract boolean performAttack(Player attacker, Vector2D direction);

    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldownMs;
    }

    @Override
    public WeaponBase clone() throws CloneNotSupportedException {
        return (WeaponBase) super.clone();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }
}
