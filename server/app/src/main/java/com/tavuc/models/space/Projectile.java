package com.tavuc.models.space;

import com.tavuc.models.GameObject;

public class Projectile extends GameObject {

    private String id;
    private float orientation; 
    private float speed; 
    private float damage;
    private String firedBy; 
    private float lifetime; 
    private long creationTime; 
    private float velocityX;
    private float velocityY;

    public Projectile(String id, int x, int y, int width, int height, float orientation, float speed, float damage, String firedBy, float lifetime) {
        super(x, y, width, height);
        this.id = id;
        this.orientation = orientation;
        this.speed = speed;
        this.damage = damage;
        this.firedBy = firedBy;
        this.lifetime = lifetime * 1000; 
        this.creationTime = System.currentTimeMillis();

        this.velocityX = (float) (Math.cos(orientation) * speed);
        this.velocityY = (float) (Math.sin(orientation) * speed);
    }

    @Override
    public void update() {
        float timeStep = 1.0f / 60.0f; 
        setX((int)(getX() + velocityX * timeStep));
        setY((int)(getY() + velocityY * timeStep));

        if (System.currentTimeMillis() - creationTime > this.lifetime) {
            //TODO: GET RID OF IT ONCE GONE

        }
    }

    public String getId() {
        return id;
    }

    public float getOrientation() {
        return orientation;
    }

    public float getSpeed() {
        return speed;
    }

    public float getDamage() {
        return damage;
    }

    public String getFiredBy() {
        return firedBy;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > lifetime;
    }
    
    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }
}
