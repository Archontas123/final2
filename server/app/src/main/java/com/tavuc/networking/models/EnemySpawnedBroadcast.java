package com.tavuc.networking.models;

public class EnemySpawnedBroadcast extends BaseMessage {
    public String enemyId;
    public String enemyType;
    public int x;
    public int y;
    public int health;
    public int width;
    public int height;

    public EnemySpawnedBroadcast(String enemyId, String enemyType, int x, int y, int health, int width, int height) {
        super();
        this.type = "ENEMY_SPAWNED_BROADCAST";
        this.enemyId = enemyId;
        this.enemyType = enemyType;
        this.x = x;
        this.y = y;
        this.health = health;
        this.width = width;
        this.height = height;
    }
}
