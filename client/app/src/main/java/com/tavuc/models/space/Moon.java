package com.tavuc.models.space;


import java.awt.Color;

import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.PlanetType; 

public class Moon {
    private int moonId;
    private PlanetType type;
    private double size; 
    private ColorPallete colorPallete; 
    private Color representativeColor; 
    private int parentPlanetId;
    private double orbitalRadius; 
    private double orbitalPeriod;
    private double currentOrbitalAngle;
    private long moonSeed;

    private double displayX;
    private double displayY;

    public Moon(int moonId, PlanetType type, double size, int parentPlanetId, double orbitalRadius, double orbitalPeriod, double currentOrbitalAngle, long moonSeed, Color representativeColor) {
        this.moonId = moonId;
        this.type = type;
        this.size = size;
        this.parentPlanetId = parentPlanetId;
        this.orbitalRadius = orbitalRadius;
        this.orbitalPeriod = orbitalPeriod;
        this.currentOrbitalAngle = currentOrbitalAngle;
        this.moonSeed = moonSeed;
        this.representativeColor = representativeColor;
    }

    /**
     * Updates the moon's display (x, y) coordinates based on its orbit around a planet.
     * @param planetX The X coordinate of the parent planet.
     * @param planetY The Y coordinate of the parent planet.
     */
    public void updateDisplayPosition(double planetX, double planetY) {
        this.displayX = planetX + this.orbitalRadius * Math.cos(this.currentOrbitalAngle);
        this.displayY = planetY + this.orbitalRadius * Math.sin(this.currentOrbitalAngle);
    }

    public int getMoonId() {
        return moonId;
    }

    public PlanetType getType() {
        return type;
    }

    public double getSize() {
        return size;
    }

    public ColorPallete getColorPallete() {
        return colorPallete;
    }

    public Color getRepresentativeColor() {
        return representativeColor;
    }

    public int getParentPlanetId() {
        return parentPlanetId;
    }

    public double getOrbitalRadius() {
        return orbitalRadius;
    }

    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public double getCurrentOrbitalAngle() {
        return currentOrbitalAngle;
    }

    public long getMoonSeed() {
        return moonSeed;
    }

    public double getDisplayX() {
        return displayX;
    }

    public double getDisplayY() {
        return displayY;
    }

    public void setMoonId(int moonId) {
        this.moonId = moonId;
    }

    public void setType(PlanetType type) {
        this.type = type;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setColorPallete(ColorPallete colorPallete) {
        this.colorPallete = colorPallete;
    }

    public void setRepresentativeColor(Color representativeColor) {
        this.representativeColor = representativeColor;
    }

    public void setParentPlanetId(int parentPlanetId) {
        this.parentPlanetId = parentPlanetId;
    }

    public void setOrbitalRadius(double orbitalRadius) {
        this.orbitalRadius = orbitalRadius;
    }

    public void setOrbitalPeriod(double orbitalPeriod) {
        this.orbitalPeriod = orbitalPeriod;
    }

    public void setCurrentOrbitalAngle(double currentOrbitalAngle) {
        this.currentOrbitalAngle = currentOrbitalAngle;
    }

    public void setMoonSeed(long moonSeed) {
        this.moonSeed = moonSeed;
    }

    public void setDisplayX(double displayX) {
        this.displayX = displayX;
    }

    public void setDisplayY(double displayY) {
        this.displayY = displayY;
    }

    public void update(double deltaTime) {
        
    }
}
