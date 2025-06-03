package com.tavuc.models.planets;

public class Chunk {

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 16;
    
    private int x;
    private int y;
    private Planet planet;
    private Tile[][] tiles;

    /**
     * Constructor for Chunk
     * @param x the leftmost x coordinate of the chunk
     * @param y the bottommost y coordinate of the chunk
     * @param planet the planet this chunk belongs to
     */
    public Chunk(int x, int y, Planet planet) {
        this.x = x;
        this.y = y;
        this.planet = planet;
        this.tiles = new Tile[16][16]; 
    }

    /**
     * get the 2D array of tiles in the chunk
     * @return
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * Gets the tile at the specified local coordinates within this chunk.
     * @param localX The local x-coordinate (0 to CHUNK_WIDTH - 1).
     * @param localY The local y-coordinate (0 to CHUNK_HEIGHT - 1).
     * @return The Tile at the given local coordinates, or null if coordinates are out of bounds.
     */
    public Tile getTileAtLocal(int localX, int localY) {
        if (localX >= 0 && localX < CHUNK_WIDTH && localY >= 0 && localY < CHUNK_HEIGHT) {
            return tiles[localX][localY];
        }
        return null; 
    }

    public java.util.List<Tile> getTilesList() {
        java.util.List<Tile> list = new java.util.ArrayList<>();
        for (int i = 0; i < CHUNK_WIDTH; i++) {
            for (int j = 0; j < CHUNK_HEIGHT; j++) {
                if (tiles[i][j] != null) {
                    list.add(tiles[i][j]);
                }
            }
        }
        return list;
    }
}
