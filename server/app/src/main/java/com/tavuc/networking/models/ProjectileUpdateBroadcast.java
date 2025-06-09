package com.tavuc.networking.models;
/**
 * Represents the ProjectileUpdateBroadcast networking message.
 */

public class ProjectileUpdateBroadcast extends BaseMessage {
    public String projectileId;
    public float x;
    public float y;
    public float velocityX;
    public float velocityY;

    public ProjectileUpdateBroadcast(String projectileId, float x, float y, float velocityX, float velocityY) {
        super();
        this.type = "PROJECTILE_UPDATE_BROADCAST";
        this.projectileId = projectileId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
}
