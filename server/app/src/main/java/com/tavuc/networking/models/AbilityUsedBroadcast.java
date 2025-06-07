package com.tavuc.networking.models;

public class AbilityUsedBroadcast extends BaseMessage {
    public String playerId;
    public String targetId;
    public String ability;

    public AbilityUsedBroadcast() {
        this.type = "ABILITY_USED_BROADCAST";
    }

    public AbilityUsedBroadcast(String playerId, String targetId, String ability) {
        this();
        this.playerId = playerId;
        this.targetId = targetId;
        this.ability = ability;
    }
}
