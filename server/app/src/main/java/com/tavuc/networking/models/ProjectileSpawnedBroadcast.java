package com.tavuc.networking.models;
/**
 * Represents the ProjectileSpawnedBroadcast networking message.
 */

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

    /**
     * Constructs a new ProjectileSpawnedBroadcast.
     */
    public ProjectileSpawnedBroadcast(String projectileId, float x, float y, int width, int height,
                                      float orientation, float speed,
                                      float velocityX, float velocityY,
                                      float damage, String firedBy) {
        super();
        this.type = "PROJECTILE_SPAWNED_BROADCAST";
        this.projectileId = projectileId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.speed = speed;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.firedBy = firedBy;
    }
}
