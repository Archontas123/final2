package com.tavuc.models.planets;


public class Tile {

    private int x;
    private int y;
    private String type;
    private ColorType colorType;

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

}
