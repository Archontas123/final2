package com.tavuc.networking.models;

public class CoinDropSpawnedBroadcast extends BaseMessage {
    public String dropId;
    public int x;
    public int y;
    public int amount;

    public CoinDropSpawnedBroadcast() {
        this.type = "COIN_DROP_SPAWNED_BROADCAST";
    }
}
