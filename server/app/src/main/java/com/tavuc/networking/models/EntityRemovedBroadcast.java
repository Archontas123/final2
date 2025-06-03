package com.tavuc.networking.models;

public class EntityRemovedBroadcast extends BaseMessage {
    public String entityId;
    public String entityType; 

    public EntityRemovedBroadcast(String entityId, String entityType) {
        super();
        this.type = "ENTITY_REMOVED_BROADCAST"; 
        this.entityId = entityId;
        this.entityType = entityType;
    }
}
