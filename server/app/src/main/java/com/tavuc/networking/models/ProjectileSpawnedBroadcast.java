package com.tavuc.networking.models;

public class ProjectileSpawnedBroadcast extends BaseMessage {
    public String projectileId;
    public float x;
    public float y;
    public int width;
    public int height;
    public float orientation;
    public float speed;
    public float damage;
    public String firedBy; 

    public ProjectileSpawnedBroadcast(String projectileId, float x, float y, int width, int height, float orientation, float speed, float damage, String firedBy) {
        super(); 
        this.type = "PROJECTILE_SPAWNED_BROADCAST"; 
        this.projectileId = projectileId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.speed = speed;
        this.damage = damage;
        this.firedBy = firedBy;
    }
}
