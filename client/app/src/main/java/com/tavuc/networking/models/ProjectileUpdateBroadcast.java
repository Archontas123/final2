package com.tavuc.networking.models;

public class ProjectileUpdateBroadcast extends BaseMessage {
    public String projectileId;
    public float x;
    public float y;
    public float velocityX;
    public float velocityY;

    public ProjectileUpdateBroadcast() {}
}
