package com.tavuc.ecs.components;

/**
 * Represents the health of an entity. 
 */
public class HealthComponent implements Component {

    /**
     * The current health value of the entity.
     */
    private float currentHealth;
    
    /**
     * The maximum possible health value for the entity.
     */
    private float maxHealth;
    
    /**
     * Constructs a new HealthComponent. The current health is initialized to the
     * maximum health.
     * @param maxHealth The maximum health value for the entity.
     */
    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    /**
     * Gets the current health of the entity.
     * @return The current health value.
     */
    public float getHealth() {
        return currentHealth;
    }
    
    /**
     * Sets the current health of the entity. The value is clamped between 0 and the
     * maximum health.
     * @param health The new health value.
     */
    public void setCurrentHealth(float health) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));
    }
    
    /**
     * Gets the maximum health of the entity.
     * @return The maximum health value.
     */
    public float getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Sets a new maximum health for the entity. If the current health exceeds this
     * new maximum, it is adjusted down to the new maximum.
     * @param maxHealth The new maximum health value.
     */
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }
    
    /**
     * Calculates the current health as a percentage of the maximum health.
     * @return The health percentage (0-100). Returns 0 if maxHealth is 0 or less to prevent division by zero.
     */
    public float getHealthPercentage() {
        return maxHealth > 0 ? (currentHealth / maxHealth) * 100f : 0f;
    }
    
    /**
     * Reduces the entity's current health by a specified amount.
     * @param amount The amount of damage to inflict.
     */
    public void takeDamage(float amount) {
        setCurrentHealth(currentHealth - amount);
    }
    
    /**
     * Increases the entity's current health by a specified amount.
     * @param amount The amount of health to restore.
     */
    public void heal(float amount) {
        setCurrentHealth(currentHealth + amount);
    }
    
    /**
     * Checks if the entity is still alive (i.e., has health greater than 0).
     * @return {@code true} if current health is greater than 0, {@code false} otherwise.
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }
}