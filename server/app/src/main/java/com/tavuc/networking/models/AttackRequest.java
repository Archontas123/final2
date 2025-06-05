package com.tavuc.networking.models;

/**
 * Client to server attack request.
 */
public class AttackRequest extends BaseMessage {
    public String playerId;
    public double directionX;
    public double directionY;

    public AttackRequest() {}

    public AttackRequest(String playerId, double directionX, double directionY) {
        this.type = "ATTACK_REQUEST";
        this.playerId = playerId;
        this.directionX = directionX;
        this.directionY = directionY;
    }
}
