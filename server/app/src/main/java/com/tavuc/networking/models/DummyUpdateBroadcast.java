package com.tavuc.networking.models;

public class DummyUpdateBroadcast extends BaseMessage {
    public int id;
    public float x;
    public float y;
    public double dx; // Include velocity if needed for client-side prediction/interpolation
    public double dy;

    public DummyUpdateBroadcast(int id, float x, float y, double dx, double dy) {
        super();
        this.type = "DUMMY_UPDATE_BROADCAST";
        this.id = id;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }
}
