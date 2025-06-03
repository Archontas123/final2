package com.tavuc.models.planets;

public class Chunk {
    
    private int x;
    private int y;
    private Tile[][] tiles;
    public static final int CHUNK_SIZE = 16;

    /**
     * Constructor for Chunk
     * @param x the leftmost x coordinate of the chunk
     * @param y the bottommost y coordinate of the chunk
     */
    public Chunk(int x, int y) {
        this.x = x;
        this.y = y;
        this.tiles = new Tile[CHUNK_SIZE][CHUNK_SIZE];
    }

    /**
     * Gets the leftmost x coordinate of the chunk
     * @return the x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the bottommost y coordinate of the chunk
     * @return the y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Get a 2D array of tiles in the chunk
     * @return a 2D array of tiles
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * Sets a tile at the specified local coordinates within the chunk
     * @param localX the local x coordinate within the chunk
     * @param localY the local y coordinate within the chunk
     * @param tile the tile to set at the specified coordinates
     */
    public void setTile(int localX, int localY, Tile tile) {
        if (localX >= 0 && localX < CHUNK_SIZE && localY >= 0 && localY < CHUNK_SIZE) {
            this.tiles[localX][localY] = tile;
        }
    }

    /**
     * Gets a tile at the specified local coordinates within the chunk
     * @param localX the local x coordinate within the chunk
     * @param localY the local y coordinate within the chunk
     * @return the tile at the specified coordinates, or null if out of bounds
     */
    public Tile getTile(int localX, int localY) {
        if (localX >= 0 && localX < CHUNK_SIZE && localY >= 0 && localY < CHUNK_SIZE) {
            return this.tiles[localX][localY];
        }
        return null;
    }
}
