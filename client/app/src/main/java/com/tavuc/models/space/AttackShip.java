package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class AttackShip extends BaseShip {

    private String aiState; 
    private double serverAttackPointX;
    private double serverAttackPointY;
    private static final int ATTACK_SHIP_WIDTH = 48; 
    private static final int ATTACK_SHIP_HEIGHT = 48;
    private static final float ATTACK_SHIP_MAX_HEALTH = 100f; 

    public AttackShip(String entityId, double x, double y, int width, int height) {
        super(entityId, x, y, width, height, ATTACK_SHIP_MAX_HEALTH);
        this.aiState = "";

    }

    public void updateNetworkState(double newX, double newY, float newOrientation, float newHealth, float newMaxHealth, String newaiState, double newServerAttackPointX, double newServerAttackPointY) {
        super.updateNetworkState(newX, newY, newOrientation, newHealth, newMaxHealth);
        this.aiState = newaiState;
        this.serverAttackPointX = newServerAttackPointX;
        this.serverAttackPointY = newServerAttackPointY;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        double drawX = this.x - offsetX;
        double drawY = this.y - offsetY;

        Graphics2D g2dCopy = (Graphics2D) g2d.create();

        g2dCopy.translate(drawX + this.width / 2.0, drawY + this.height / 2.0);
        g2dCopy.rotate(this.orientation);
        g2dCopy.translate(-this.width / 2.0, -this.height / 2.0);

        g2dCopy.setColor(Color.ORANGE); 
        g2dCopy.fillRect(0, 0, this.width, this.height);
        
        g2dCopy.dispose();
    }

    public String getaiState() {
        return aiState;
    }

    public void setaiState(String aiState) {
        this.aiState = aiState;
    }

    public double getServerAttackPointX() {
        return serverAttackPointX;
    }

    public void setServerAttackPointX(double serverAttackPointX) {
        this.serverAttackPointX = serverAttackPointX;
    }

    public double getServerAttackPointY() {
        return serverAttackPointY;
    }

    public void setServerAttackPointY(double serverAttackPointY) {
        this.serverAttackPointY = serverAttackPointY;
    }
}
