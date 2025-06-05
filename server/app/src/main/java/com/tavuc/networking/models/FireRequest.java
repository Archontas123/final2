package com.tavuc.networking.models;

public class FireRequest extends BaseMessage {
    public String playerId;
    public double shipX;
    public double shipY;
    public double shipAngle;
    public double shipDx;
    public double shipDy;

    public FireRequest() {
        this.type = "FIRE_REQUEST";
    }
}
