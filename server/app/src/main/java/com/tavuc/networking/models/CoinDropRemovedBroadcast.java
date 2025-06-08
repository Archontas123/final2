package com.tavuc.networking.models;

public class CoinDropRemovedBroadcast extends BaseMessage {
    public String dropId;

    public CoinDropRemovedBroadcast(String dropId) {
        super();
        this.type = "COIN_DROP_REMOVED_BROADCAST";
        this.dropId = dropId;
    }
}
