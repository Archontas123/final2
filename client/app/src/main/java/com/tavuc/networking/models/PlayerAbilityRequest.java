package com.tavuc.networking.models;

public class PlayerAbilityRequest extends BaseMessage {
    public String casterId;
    public String targetId;
    public int abilityType; // 1=freeze,2=push,3=pull

    public PlayerAbilityRequest() {
        this.type = "PLAYER_ABILITY_REQUEST";
    }

    public PlayerAbilityRequest(String casterId, String targetId, int abilityType) {
        this();
        this.casterId = casterId;
        this.targetId = targetId;
        this.abilityType = abilityType;
    }
}
