package com.tavuc.models.planets;

import javax.swing.JComponent;


import java.awt.Rectangle;

public class Tile {

    public static final int TILE_WIDTH = 16;
    public static final int TILE_HEIGHT = 16;

    private int x;
    private int y;
    private Planet planet;
    private String type;
    private ColorType colorType;
    private Rectangle hitbox;
    private boolean solid;

    /**
     * Constructor for Tile
     * @param x the x coordinate of the tile (world coordinates)
     * @param y the y coordinate of the tile (world coordinates)
     * @param planet the planet this tile belongs to
     * @param type the type of the tile (e.g., "grass", "water")
     * @param colorType the color type of the tile
     */
    public Tile(int x, int y, Planet planet, String type, ColorType colorType) {
        this.colorType = colorType;
        this.x = x;
        this.y = y;
        this.planet = planet;
        this.type = type;
        this.hitbox = new Rectangle(x, y, TILE_WIDTH, TILE_HEIGHT);
        // Only certain tile types are solid for collision purposes. For now
        // treat "rock" tiles as impassable terrain.
        this.solid = "rock".equalsIgnoreCase(type);
    }

    /**
     * Checks if the tile is solid for collision purposes.
     * @return true if the tile is solid, false otherwise.
     */
    public boolean isSolid() {
        return solid;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    /**
     * Gets the hitbox of the tile.
     * @return The hitbox of the tile.
     */
    public Rectangle getHitbox() {
        return this.hitbox.getBounds(); // Hitbox might still be needed for other purposes
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

    /**
     * Gets the width of the tile.
     * @return The width of the tile.
     */
    public int getWidth() {
        return TILE_WIDTH;
    }

    /**
     * Gets the height of the tile.
     * @return The height of the tile.
     */
    public int getHeight() {
        return TILE_HEIGHT;
    }

    /**
     * Gets the hitbox of the tile.
     * @return The hitbox of the tile.
     */
    public Rectangle getHitBox() {
        return this.hitbox.getBounds();
    }
}
