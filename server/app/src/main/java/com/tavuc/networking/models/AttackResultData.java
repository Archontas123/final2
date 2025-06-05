package com.tavuc.networking.models;

public class AttackResultData {
    public String playerId;
    public float damageDealt;
    public float newHealth;
    public boolean blocked;

    public AttackResultData(String playerId, float damageDealt, float newHealth, boolean blocked) {
        this.playerId = playerId;
        this.damageDealt = damageDealt;
        this.newHealth = newHealth;
        this.blocked = blocked;
    }

    public AttackResultData() {}
}
