package com.tavuc.networking.models;

public class DummyRemovedBroadcast extends BaseMessage {
    public int id;

    public DummyRemovedBroadcast(int id) {
        super();
        this.type = "DUMMY_REMOVED_BROADCAST";
        this.id = id;
    }
}
