package com.tavuc.networking.models;

public class EnemyUpdateBroadcast extends BaseMessage {
    public String enemyId;
    public int x;
    public int y;
    public double dx;
    public double dy;
    public double direction;
    public double health;

    public EnemyUpdateBroadcast(String enemyId, int x, int y, double dx, double dy, double direction, double health) {
        super();
        this.type = "ENEMY_UPDATE_BROADCAST";
        this.enemyId = enemyId;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.direction = direction;
        this.health = health;
    }
}
