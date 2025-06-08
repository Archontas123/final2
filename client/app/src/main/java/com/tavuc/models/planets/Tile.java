package com.tavuc.models.planets;


public class Tile {

    private int x;
    private int y;
    private String type;
    private ColorType colorType;
    private boolean solid;
    private double friction;

    /**
     * Constructor for Tile
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @param type the type of the tile (e.g., "grass", "water")
     * @param colorType the color type of the tile
     */
    public Tile(int x, int y, String type, ColorType colorType) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.colorType = colorType;
        this.solid = "rock".equalsIgnoreCase(type);
        switch(type.toUpperCase()) {
            case "WATER": friction = 0.9; break;
            case "DIRT": friction = 0.82; break;
            case "ICE": friction = 0.98; break;
            default: friction = 0.85; break;
        }
    }

    /**
     * Gets the x coordinate of the tile
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y coordinate of the tile
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the type of the tile
     * @return the type of the tile
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the color type of the tile
     * @return the color type of the tile
     */
    public ColorType getColorType() {
        return colorType;
    }

    public boolean isSolid() {
        return solid;
    }

    public double getFriction() {
        return friction;
    }

}
