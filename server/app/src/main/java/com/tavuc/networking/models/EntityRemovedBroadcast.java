package com.tavuc.networking.models;
/**
 * Represents the EntityRemovedBroadcast networking message.
 */

public class EntityRemovedBroadcast extends BaseMessage {
    public String entityId;
    public String entityType;

    /**
     * Constructs a new EntityRemovedBroadcast.
     */
    public EntityRemovedBroadcast(String entityId, String entityType) {
        super();
        this.type = "ENTITY_REMOVED_BROADCAST";
        this.entityId = entityId;
        this.entityType = entityType;
    }
}
