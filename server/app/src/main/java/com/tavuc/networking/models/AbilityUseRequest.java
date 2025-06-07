package com.tavuc.networking.models;

public class AbilityUseRequest extends BaseMessage {
    public String playerId;
    public String targetId;
    public String ability;

    public AbilityUseRequest() {
        this.type = "ABILITY_USE_REQUEST";
    }

    public AbilityUseRequest(String playerId, String targetId, String ability) {
        this();
        this.playerId = playerId;
        this.targetId = targetId;
        this.ability = ability;
    }
}
