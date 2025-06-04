package com.tavuc.ecs.components;

public class HealthComponent implements Component {
    private float currentHealth;
    private float maxHealth;
    
    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }
    
    public float getHealth() {
        return currentHealth;
    }
    
    public void setCurrentHealth(float health) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));
    }
    
    public float getMaxHealth() {
        return maxHealth;
    }
    
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }
    
    public float getHealthPercentage() {
        return maxHealth > 0 ? (currentHealth / maxHealth) * 100f : 0f;
    }
    
    public void takeDamage(float amount) {
        setCurrentHealth(currentHealth - amount);
    }
    
    public void heal(float amount) {
        setCurrentHealth(currentHealth + amount);
    }
    
    public boolean isAlive() {
        return currentHealth > 0;
    }
}
