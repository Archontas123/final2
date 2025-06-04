package com.tavuc.ecs.components;

public class ShieldComponent implements Component {
    private float currentShield;
    private float maxShield;
    private float rechargeRate;
    private float rechargeDelay;
    private long lastDamageTime;
    
    public ShieldComponent(float maxShield, float rechargeRate, float rechargeDelay) {
        this.maxShield = maxShield;
        this.currentShield = maxShield;
        this.rechargeRate = rechargeRate;
        this.rechargeDelay = rechargeDelay;
        this.lastDamageTime = 0;
    }

    public ShieldComponent(float maxShield) {
        this(maxShield, 5f, 3f); // Default rechargeRate and rechargeDelay
    }
    
    public float getShield() {
        return currentShield;
    }
    
    public void setCurrentShield(float shield) {
        this.currentShield = Math.max(0, Math.min(shield, maxShield));
    }
    
    public float getMaxShield() {
        return maxShield;
    }
    
    public void setMaxShield(float maxShield) {
        this.maxShield = maxShield;
        if (currentShield > maxShield) {
            currentShield = maxShield;
        }
    }
    
    public float getShieldPercentage() {
        return maxShield > 0 ? (currentShield / maxShield) * 100f : 0f;
    }
    
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
    
    public void update(float deltaTime) {
        if (currentShield < maxShield && 
            System.currentTimeMillis() - lastDamageTime > rechargeDelay * 1000) {
            setCurrentShield(currentShield + rechargeRate * deltaTime);
        }
    }
    
    public boolean hasShield() {
        return currentShield > 0;
    }
}
