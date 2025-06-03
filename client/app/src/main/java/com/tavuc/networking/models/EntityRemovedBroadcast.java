package com.tavuc.networking.models;

public class EntityRemovedBroadcast extends BaseMessage {
    public String entityId;
    public String entityType; 

    public EntityRemovedBroadcast() {
        super();
    }
}
