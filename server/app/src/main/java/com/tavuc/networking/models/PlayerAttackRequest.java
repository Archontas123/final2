package com.tavuc.networking.models;
/**
 * Represents the PlayerAttackRequest networking message.
 */

public class PlayerAttackRequest extends BaseMessage {
    public String attackerId;
    public String targetId;
    public double damage;

    public PlayerAttackRequest() {
        this.type = "PLAYER_ATTACK_REQUEST";
    }

    public PlayerAttackRequest(String attackerId, String targetId, double damage) {
        this();
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.damage = damage;
    }
}
