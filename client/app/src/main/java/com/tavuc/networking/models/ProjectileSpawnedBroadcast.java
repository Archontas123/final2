package com.tavuc.networking.models;

public class ProjectileSpawnedBroadcast extends BaseMessage {
    public String projectileId;
    public float x;
    public float y;
    public int width;
    public int height;
    public float orientation;
    public float speed;
    public float velocityX;
    public float velocityY;
    public float damage;
    public String firedBy;

    // Default constructor for Gson
    public ProjectileSpawnedBroadcast() {
    }
}
