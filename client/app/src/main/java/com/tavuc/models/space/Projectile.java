package com.tavuc.models.space;

public class Projectile {
    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double damage;
    private String ownerId;
    private boolean active;

    public Projectile(double x, double y, double velocityX, double velocityY, double damage, String ownerId) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.ownerId = ownerId;
        this.active = true;
    }

    public void tick(double delta) {
        this.x += velocityX * delta;
        this.y += velocityY * delta;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDamage() {
        return damage;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
