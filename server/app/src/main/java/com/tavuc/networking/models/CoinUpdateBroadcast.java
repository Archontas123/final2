package com.tavuc.networking.models;

public class CoinUpdateBroadcast extends BaseMessage {
    public String playerId;
    public int coins;

    public CoinUpdateBroadcast() {
        this.type = "COIN_UPDATE_BROADCAST";
    }

    public CoinUpdateBroadcast(String playerId, int coins) {
        this();
        this.playerId = playerId;
        this.coins = coins;
    }
}
