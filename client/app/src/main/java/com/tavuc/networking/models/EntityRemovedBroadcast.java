package com.tavuc.networking.models;
/**
 * Represents the EntityRemovedBroadcast networking message.
 */

public class EntityRemovedBroadcast extends BaseMessage {
    public String entityId;
    public String entityType;

    public EntityRemovedBroadcast() {
        super();
    }
}
