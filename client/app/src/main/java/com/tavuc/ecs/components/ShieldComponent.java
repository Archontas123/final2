package com.tavuc.ecs.components;

/**
 * Represents a rechargeable shield for an entity. 
 */
public class ShieldComponent implements Component {

    /**
     * The current strength of the shield.
     */
    private float currentShield;
    /**
     * The maximum possible strength of the shield.
     */
    private float maxShield;
    /**
     * The rate at which the shield recharges per second.
     */
    private float rechargeRate;
    /**
     * The delay in seconds after taking damage before the shield begins to recharge.
     */
    private float rechargeDelay;
    /**
     * The system time when the shield last took damage.
     */
    private long lastDamageTime;
    
    /**
     * Constructs a new ShieldComponent with specified parameters.
     * @param maxShield The maximum strength of the shield.
     * @param rechargeRate The amount of shield points to regenerate per second.
     * @param rechargeDelay The time in seconds to wait after taking damage before recharging starts.
     */
    public ShieldComponent(float maxShield, float rechargeRate, float rechargeDelay) {
        this.maxShield = maxShield;
        this.currentShield = maxShield;
        this.rechargeRate = rechargeRate;
        this.rechargeDelay = rechargeDelay;
        this.lastDamageTime = 0;
    }

    /**
     * Constructs a new ShieldComponent with a specified maximum strength and default
     * recharge rate and delay.
     * @param maxShield The maximum strength of the shield.
     */
    public ShieldComponent(float maxShield) {
        this(maxShield, 5f, 3f); 
    }
    
    /**
     * Gets the current strength of the shield.
     * @return The current shield value.
     */
    public float getShield() {
        return currentShield;
    }
    
    /**
     * Sets the current strength of the shield. The value is clamped between 0 and the
     * maximum shield strength.
     * @param shield The new shield value.
     */
    public void setCurrentShield(float shield) {
        this.currentShield = Math.max(0, Math.min(shield, maxShield));
    }
    
    /**
     * Gets the maximum strength of the shield.
     * @return The maximum shield value.
     */
    public float getMaxShield() {
        return maxShield;
    }
    
    /**
     * Sets a new maximum strength for the shield. If the current shield strength
     * exceeds this new maximum, it is adjusted down to the new maximum.
     * @param maxShield The new maximum shield value.
     */
    public void setMaxShield(float maxShield) {
        this.maxShield = maxShield;
        if (currentShield > maxShield) {
            currentShield = maxShield;
        }
    }
    
    /**
     * Calculates the current shield strength as a percentage of the maximum strength.
     * @return The shield percentage (0-100). Returns 0 if maxShield is 0 or less.
     */
    public float getShieldPercentage() {
        return maxShield > 0 ? (currentShield / maxShield) * 100f : 0f;
    }
    
    /**
     * Applies damage to the shield. The shield absorbs as much damage as it can.
     * Taking damage resets the recharge delay timer.
     * @param amount The incoming damage amount.
     * @return The amount of damage remaining after the shield has absorbed its share.
     *         This "overflow" damage should be applied to the entity's health.
     */
    public float takeDamage(float amount) {
        float damageToTake = amount;
        float remainingDamage = 0;

        if (currentShield >= damageToTake) {
            setCurrentShield(currentShield - damageToTake);
            damageToTake = 0;
        } else {
            remainingDamage = damageToTake - currentShield;
            setCurrentShield(0);
        }
        lastDamageTime = System.currentTimeMillis();
        return remainingDamage;
    }
    
    /**
     * Updates the shield's state, typically called once per frame. It handles the
     * recharging logic based on the time since the last damage was taken.
     * @param deltaTime The time in seconds that has passed since the last update.
     */
    public void update(float deltaTime) {
        if (currentShield < maxShield && 
            System.currentTimeMillis() - lastDamageTime > rechargeDelay * 1000) {
            setCurrentShield(currentShield + rechargeRate * deltaTime);
        }
    }
    
    /**
     * Checks if the shield has any strength remaining.
     * @return {@code true} if the current shield is greater than 0, {@code false} otherwise.
     */
    public boolean hasShield() {
        return currentShield > 0;
    }
}