package com.tavuc.networking.models;
/**
 * Represents the ProjectileRemovedBroadcast networking message.
 */

public class ProjectileRemovedBroadcast extends BaseMessage {
    public String projectileId;

    /**
     * Constructs a new ProjectileRemovedBroadcast.
     */
    public ProjectileRemovedBroadcast(String projectileId) {
        super();
        this.type = "PROJECTILE_REMOVED_BROADCAST";
        this.projectileId = projectileId;
    }
}
