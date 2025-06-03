package com.tavuc.models.planets;

public class Moon {
    private int moonId;
    private String moonName;
    private PlanetType type;
    private double size;
    private ColorPallete colorPallete;
    private int parentPlanetId;
    private double orbitalRadius; 
    private double orbitalPeriod;
    private double currentOrbitalAngle;
    private long moonSeed;

    public Moon(int moonId, String moonName, PlanetType type, double size, ColorPallete colorPallete,
                int parentPlanetId, double orbitalRadius, double orbitalPeriod,
                double currentOrbitalAngle, long moonSeed) {
        this.moonId = moonId;
        this.moonName = moonName;
        this.type = type;
        this.size = size;
        this.colorPallete = colorPallete;
        this.parentPlanetId = parentPlanetId;
        this.orbitalRadius = orbitalRadius;
        this.orbitalPeriod = orbitalPeriod;
        this.currentOrbitalAngle = currentOrbitalAngle;
        this.moonSeed = moonSeed;
    }

    public int getMoonId() {
        return moonId;
    }

    public void setMoonId(int moonId) {
        this.moonId = moonId;
    }

    public String getMoonName() {
        return moonName;
    }

    public void setMoonName(String moonName) {
        this.moonName = moonName;
    }

    public PlanetType getType() {
        return type;
    }

    public void setType(PlanetType type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public ColorPallete getColorPallete() {
        return colorPallete;
    }

    public void setColorPallete(ColorPallete colorPallete) {
        this.colorPallete = colorPallete;
    }

    public int getParentPlanetId() {
        return parentPlanetId;
    }

    public void setParentPlanetId(int parentPlanetId) {
        this.parentPlanetId = parentPlanetId;
    }

    public double getOrbitalRadius() {
        return orbitalRadius;
    }

    public void setOrbitalRadius(double orbitalRadius) {
        this.orbitalRadius = orbitalRadius;
    }

    public double getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public void setOrbitalPeriod(double orbitalPeriod) {
        this.orbitalPeriod = orbitalPeriod;
    }

    public double getCurrentOrbitalAngle() {
        return currentOrbitalAngle;
    }

    public void setCurrentOrbitalAngle(double currentOrbitalAngle) {
        this.currentOrbitalAngle = currentOrbitalAngle;
    }

    public long getMoonSeed() {
        return moonSeed;
    }

    public void setMoonSeed(long moonSeed) {
        this.moonSeed = moonSeed;
    }

    /**
     * Formats the Moon data into a string for client communication.
     * Format: MOON:<moonId>:<name>:<type_str>:<size_approx>:<color_rgb_primary>:<orbR>
     * @return A string representation of the moon for the client.
     */
    public String toClientStringFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("MOON:");
        sb.append(moonId).append(":");
        sb.append(moonName).append(":");
        sb.append(type != null ? type.name() : "UNKNOWN").append(":");
        sb.append(size).append(":");

        if (colorPallete != null && colorPallete.getPrimarySurface() != null) {
            java.awt.Color primaryColor = colorPallete.getPrimarySurface(); 
            sb.append(primaryColor.getRed()).append(",").append(primaryColor.getGreen()).append(",").append(primaryColor.getBlue());
        } else {
            sb.append("100,100,100"); 
        }
        sb.append(":").append(orbitalRadius);
        return sb.toString();
    }
}
