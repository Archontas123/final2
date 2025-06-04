package com.tavuc.networking.models;

public class ProjectileRemovedBroadcast extends BaseMessage {
    public String projectileId;

    public ProjectileRemovedBroadcast(String projectileId) {
        super();
        this.type = "PROJECTILE_REMOVED_BROADCAST";
        this.projectileId = projectileId;
    }
}
