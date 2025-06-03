package com.tavuc.networking.models;

public class DummyUpdateBroadcast extends BaseMessage {
    public int id;
    public float x;
    public float y;
    // Add other relevant fields like dx, dy if dummies move smoothly on client
    // or if other state needs to be synced.

    public DummyUpdateBroadcast(int id, float x, float y) {
        super();
        this.type = "DUMMY_UPDATE_BROADCAST";
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
