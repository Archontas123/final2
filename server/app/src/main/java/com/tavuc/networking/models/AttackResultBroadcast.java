package com.tavuc.networking.models;

import java.util.List;

public class AttackResultBroadcast extends BaseMessage {
    public String attackerId;
    public double directionX;
    public double directionY;
    public List<AttackResultData> results;

    public AttackResultBroadcast(String attackerId, double directionX, double directionY, List<AttackResultData> results) {
        this.type = "ATTACK_RESULT_BROADCAST";
        this.attackerId = attackerId;
        this.directionX = directionX;
        this.directionY = directionY;
        this.results = results;
    }
}
