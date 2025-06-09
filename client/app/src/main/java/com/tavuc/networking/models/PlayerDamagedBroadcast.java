package com.tavuc.networking.models;
/**
 * Represents the PlayerDamagedBroadcast networking message.
 */

public class PlayerDamagedBroadcast extends BaseMessage {
    public String playerId;
    public double damage;
    public double currentHealth;

    /**
     * Constructs a new PlayerDamagedBroadcast.
     */
    public PlayerDamagedBroadcast() {
        this.type = "PLAYER_DAMAGED_BROADCAST";
    }

    /**
     * Constructs a new PlayerDamagedBroadcast.
     */
    public PlayerDamagedBroadcast(String playerId, double damage, double currentHealth) {
        this();
        this.playerId = playerId;
        this.damage = damage;
        this.currentHealth = currentHealth;
    }
}
