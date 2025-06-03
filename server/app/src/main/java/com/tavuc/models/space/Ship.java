package com.tavuc.models.space;

public class Ship {
    private int playerId;
    private double x;
    private double y;
    private double angle;
    private double dx;
    private double dy;
    private boolean thrusting;
    private boolean inSpace;

    public Ship(int playerId, double x, double y, double angle, double dx, double dy, boolean thrusting) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.dx = dx;
        this.dy = dy;
        this.thrusting = thrusting;
        this.inSpace = true; 
    }

    public int getPlayerId() {
        return playerId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getDx() {
        return dx;
    }

    public void setDx(double dx) {
        this.dx = dx;
    }

    public double getDy() {
        return dy;
    }

    public void setDy(double dy) {
        this.dy = dy;
    }

    public boolean isThrusting() {
        return thrusting;
    }

    public void setThrusting(boolean thrusting) {
        this.thrusting = thrusting;
    }

    public boolean isInSpace() {
        return inSpace;
    }

    public void setInSpace(boolean inSpace) {
        this.inSpace = inSpace;
    }
}
