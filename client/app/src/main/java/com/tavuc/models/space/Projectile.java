package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics2D;
import com.tavuc.models.GameObject;

public class Projectile extends GameObject {
    private String entityId; 
    private float orientation;
    private float speed;
    private String firedBy; 
    private long spawnTime;
    private float lifetimeMillis; 
    private boolean active;

    public Projectile(String id, int x, int y, int width, int height, float orientation, float speed, String firedBy, float lifetimeSeconds) {
        super(x, y, width, height); 
        this.entityId = id; 
        this.orientation = orientation;
        this.speed = speed;
        this.firedBy = firedBy;
        this.spawnTime = System.currentTimeMillis();
        this.lifetimeMillis = lifetimeSeconds * 1000;
        this.active = true;
    }

    public void update() {
        if (!active) return;

        if (System.currentTimeMillis() - spawnTime > lifetimeMillis) {
            active = false;
            return;
        }

        float timeStep = 1.0f / 60.0f; 
        
        double dx = speed * Math.cos(orientation) * timeStep;
        double dy = speed * Math.sin(orientation) * timeStep;

        setX(getX() + (int)dx);
        setY(getY() + (int)dy);
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        if (!active) return;
        
        g2d.setColor(Color.YELLOW); 
        g2d.fillOval(getX() - getWidth()/2, getY() - getHeight()/2, getWidth(), getHeight());
    }

    public boolean isActive() {
        return active;
    }

    public String getFiredBy() {
        return firedBy;
    }

    public String getId() {
        return entityId;
    }
}
