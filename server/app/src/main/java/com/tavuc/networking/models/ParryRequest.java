package com.tavuc.networking.models;

public class ParryRequest extends BaseMessage {
    public String playerId;

    public ParryRequest() {}

    public ParryRequest(String playerId) {
        this.type = "PARRY_REQUEST";
        this.playerId = playerId;
    }
}
