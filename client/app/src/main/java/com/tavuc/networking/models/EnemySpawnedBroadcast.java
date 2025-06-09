package com.tavuc.networking.models;

public class EnemySpawnedBroadcast extends BaseMessage {
    public String enemyId;
    public String enemyType;
    public int x;
    public int y;
    public int health;
    public int width;
    public int height;

    public EnemySpawnedBroadcast() {
        super();
    }
}
