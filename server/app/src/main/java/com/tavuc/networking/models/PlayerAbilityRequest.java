package com.tavuc.networking.models;

public class PlayerAbilityRequest extends BaseMessage {
    public String casterId;
    public String targetId;
    // 1=freeze,2=push,3=pull,4=dash,5=shield,6=lightning,7=choke,8=heal,9=slam,10=cloak
    public int abilityType;

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
