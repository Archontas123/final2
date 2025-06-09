package com.tavuc.networking.models;
/**
 * Represents the PlanetInfo networking message.
 */

public class PlanetInfo {
    public String gameId;
    public String planetName;
    public double x;
    public double y;
    public double size;
    public String type;
    public int hueShiftColor;

    public PlanetInfo() {
    }

    public PlanetInfo(String gameId, String planetName, double x, double y, double size, String type, int hueShiftColor) {
        this.gameId = gameId;
        this.planetName = planetName;
        this.x = x;
        this.y = y;
        this.size = size;
        this.type = type;
        this.hueShiftColor = hueShiftColor;
    }
}
