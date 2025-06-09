package com.tavuc.networking.models;

public class EnemyUpdateBroadcast extends BaseMessage {
    public String enemyId;
    public int x;
    public int y;
    public double dx;
    public double dy;
    public double direction;
    public double health;

    public EnemyUpdateBroadcast() {
        super();
    }
}
