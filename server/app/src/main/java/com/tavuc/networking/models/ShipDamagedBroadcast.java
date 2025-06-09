package com.tavuc.networking.models;
/**
 * Represents the ShipDamagedBroadcast networking message.
 */


public class ShipDamagedBroadcast extends BaseMessage {
    public String playerId;
    public float damageAmount;
    public float currentHealth;
    public float maxHealth;
    public String damageDealerId;

    /**
     * Constructs a new ShipDamagedBroadcast.
     */
    public ShipDamagedBroadcast() {
        this.type = "SHIP_DAMAGED_BROADCAST";
    }

    /**
     * Constructs a new ShipDamagedBroadcast.
     */
    public ShipDamagedBroadcast(String playerId, float damageAmount, float currentHealth,
                                float maxHealth, String damageDealerId) {
        this.type = "SHIP_DAMAGED_BROADCAST";
        this.playerId = playerId;
        this.damageAmount = damageAmount;
        this.currentHealth = currentHealth;
        this.maxHealth = maxHealth;
        this.damageDealerId = damageDealerId;
    }
}
