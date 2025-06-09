package com.tavuc.networking.models;

public class EnemyRemovedBroadcast extends BaseMessage {
    public String enemyId;

    public EnemyRemovedBroadcast(String enemyId) {
        super();
        this.type = "ENEMY_REMOVED_BROADCAST";
        this.enemyId = enemyId;
    }
}
