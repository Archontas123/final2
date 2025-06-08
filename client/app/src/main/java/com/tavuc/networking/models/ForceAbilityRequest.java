package com.tavuc.networking.models;

public class ForceAbilityRequest extends BaseMessage {
    public String attackerId;
    public String targetId;
    public String ability;

    public ForceAbilityRequest() {
        this.type = "FORCE_ABILITY_REQUEST";
    }

    public ForceAbilityRequest(String attackerId, String targetId, String ability) {
        this();
        this.attackerId = attackerId;
        this.targetId = targetId;
        this.ability = ability;
    }
}
