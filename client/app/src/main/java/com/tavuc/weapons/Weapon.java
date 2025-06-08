package com.tavuc.weapons;

import com.tavuc.models.entities.Player;
import com.tavuc.utils.Vector2D;

/**
 * Base class for all weapons.
 */
public abstract class Weapon {
    protected WeaponType type;
    protected WeaponStats stats;
    protected AnimationController animations;
    protected SoundController sounds;
    protected EffectsController effects;
    protected CooldownManager cooldowns = new CooldownManager();

    public Weapon(WeaponType type, WeaponStats stats) {
        this.type = type;
        this.stats = stats;
        this.animations = new AnimationController();
        this.sounds = new SoundController();
        this.effects = new EffectsController();
    }

    public WeaponType getType() {
        return type;
    }

    public WeaponStats getStats() {
        return stats;
    }

    public abstract void primaryAttack(Player wielder, Vector2D targetPosition);
    public abstract void secondaryAttack(Player wielder);
    public abstract boolean canAttack();
}
