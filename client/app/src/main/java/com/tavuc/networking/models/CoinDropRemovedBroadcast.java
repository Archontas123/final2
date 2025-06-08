package com.tavuc.networking.models;

public class CoinDropRemovedBroadcast extends BaseMessage {
    public String dropId;

    public CoinDropRemovedBroadcast() {
        this.type = "COIN_DROP_REMOVED_BROADCAST";
    }
}
