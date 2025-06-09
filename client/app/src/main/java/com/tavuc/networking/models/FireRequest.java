
package com.tavuc.networking.models;
/**
 * Represents the FireRequest networking message.
 */

public class FireRequest extends BaseMessage {
    public String playerId;
    public double shipX;
    public double shipY;
    public double shipAngle;
    public double shipDx;
    public double shipDy;

    public FireRequest() {
        super();
        this.type = "FIRE_REQUEST";
    }


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
