package com.tavuc.networking.models;

public class CoinDropSpawnedBroadcast extends BaseMessage {
    public String dropId;
    public int x;
    public int y;
    public int amount;

    public CoinDropSpawnedBroadcast(String dropId, int x, int y, int amount) {
        super();
        this.type = "COIN_DROP_SPAWNED_BROADCAST";
        this.dropId = dropId;
        this.x = x;
        this.y = y;
        this.amount = amount;
    }
}
