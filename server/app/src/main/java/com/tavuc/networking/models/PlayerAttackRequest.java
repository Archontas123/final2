package com.tavuc.networking.models;
/**
 * Represents the PlayerAttackRequest networking message.
 */

public class PlayerAttackRequest extends BaseMessage {
    public String attackerId;
    public String targetId;
    public double damage;

    /**
     * Constructs a new PlayerAttackRequest.
     */
    public PlayerAttackRequest() {
        this.type = "PLAYER_ATTACK_REQUEST";
    }

    /**
     * Constructs a new PlayerAttackRequest.
     */
    public PlayerAttackRequest(String attackerId, String targetId, double damage) {
        this();
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.damage = damage;
    }
}
