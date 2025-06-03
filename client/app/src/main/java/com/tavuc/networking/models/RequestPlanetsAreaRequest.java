package com.tavuc.networking.models;

public class RequestPlanetsAreaRequest extends BaseMessage {
    public double centerX;
    public double centerY;
    public double radius;

    public RequestPlanetsAreaRequest(double centerX, double centerY, double radius) {
        this.type = "REQUEST_PLANETS_AREA_REQUEST";
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }
}
