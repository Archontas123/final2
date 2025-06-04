// server/app/src/main/java/com/tavuc/networking/models/FireRequest.java

package com.tavuc.networking.models;

public class FireRequest extends BaseMessage {
    public String playerId; // Add player ID field
    public double shipX;    // Ship position fields for more accurate projectile spawning
    public double shipY;
    public double shipAngle;
    public double shipDx;
    public double shipDy;

    public FireRequest() {
        super(); 
        this.type = "FIRE_REQUEST"; // Change to uppercase with underscore to match server expectations
    }
    
    // Add constructor with player data
    public FireRequest(String playerId, double shipX, double shipY, double shipAngle, double shipDx, double shipDy) {
        this();
        this.playerId = playerId;
        this.shipX = shipX;
        this.shipY = shipY;
        this.shipAngle = shipAngle;
        this.shipDx = shipDx;
        this.shipDy = shipDy;
    }
}