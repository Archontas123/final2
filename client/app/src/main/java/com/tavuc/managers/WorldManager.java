package com.tavuc.managers;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.tavuc.Client;
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.ColorType;
import com.tavuc.models.planets.Tile;
import com.tavuc.networking.models.RequestChunkResponse;
import com.tavuc.networking.models.TileData;



public class WorldManager {
    
    private Map<Point, Chunk> chunks;
    private ColorPallete palette;
    private int gameId; 
    public static final int TILE_SIZE = 32; 
    private static final int chunkLoadRadius = 2; 
    private static final Gson gson = new Gson(); 

    /**
     * Constructor for WorldManager 
     * @param gameId the game ID to assign to the WorldManager
     */
    public WorldManager(int gameId) {
        this.gameId = gameId;
        this.chunks = new ConcurrentHashMap<>();
    }

    /**
     * Sets the game ID for the WorldManager
     * @param gameId
     */
    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    /**
     * Sets the current color palette for the WorldManager
     * @param palette
     */
    public void setPallete(ColorPallete palette) {
        this.palette = palette;
    }

    /**
     * Gets the current color palette for the WorldManager
     * @return the current color palette
     */
    public ColorPallete getCurrentPalette() {
        return palette;
    }

    /**
     * Gets the loaded chunks for the WorldManager
     * @return the loaded chunks
     */
    public Map<Point, Chunk> getChunks() {
        return chunks;
    }

    /**
     * Gets the chunk at the specified coordinates
     * @param x the leftmost x coordinate of the chunk
     * @param y the bottommost y coordinate of the chunk
     * @return the chunk at the specified coordinates
     */
    public Chunk getChunk(int x, int y) {
        return chunks.get(new Point(x, y));
    }


    /**
     * Processes the chunk data received from the server
     * @param response the RequestChunkResponse object containing the chunk data
     */
    public void processChunkData(RequestChunkResponse response) {
        if (response == null || response.tiles == null) {
            System.err.println("WorldManager: Received null or invalid chunk data response.");
            return;
        }

        try {
            int chunkX = response.chunkX;
            int chunkY = response.chunkY;

            Chunk chunk = chunks.computeIfAbsent(new Point(chunkX, chunkY), k -> new Chunk(chunkX, chunkY));

            for (TileData tileData : response.tiles) {
                try {
                    ColorType colorType = ColorType.valueOf(tileData.colorTypeName);
                    int globalX = tileData.x; 
                    int globalY = tileData.y;

                    int localX = globalX - (chunkX * Chunk.CHUNK_SIZE);
                    int localY = globalY - (chunkY * Chunk.CHUNK_SIZE);

                    Tile clientTile = new Tile(globalX, globalY, tileData.tileType, colorType);
                    chunk.setTile(localX, localY, clientTile);

                } catch (IllegalArgumentException e) {
                    System.err.println("WorldManager: Error parsing ColorType from TileData: " + tileData.colorTypeName + " - " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("WorldManager: Error processing individual tile data: " + gson.toJson(tileData) + " - " + e.getMessage());
                }
            }
            if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
        } catch (Exception e) {
            System.err.println("WorldManager: Error processing chunk data response: " + gson.toJson(response) + " - " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Updates the visible chunks based on the player's position and view size
     * @param playerX the x coordinate of the player
     * @param playerY the y coordinate of the player
     * @param viewWidth the width of the view area
     * @param viewHeight the height of the view area
     */
    public void updateVisibleChunks(int playerX, int playerY, int viewWidth, int viewHeight) {
        int playerChunkX = playerX / (Chunk.CHUNK_SIZE * TILE_SIZE);
        int playerChunkY = playerY / (Chunk.CHUNK_SIZE * TILE_SIZE);

        for (int cx = playerChunkX - chunkLoadRadius; cx <= playerChunkX + chunkLoadRadius; cx++) {
            for (int cy = playerChunkY - chunkLoadRadius; cy <= playerChunkY + chunkLoadRadius; cy++) {
                Point chunkPos = new Point(cx, cy);
                if (!chunks.containsKey(chunkPos)) {
                    try {
                        Client.requestChunkData(this.gameId, cx, cy);
                    } catch (Exception e) {
                        System.err.println("WorldManager: Error requesting chunk " + cx + "," + cy + ": " + e.getMessage());
                    }
                }
            }
        }
        // TODO: Implement chunk unloading for chunks far from the player
        

    }

    /**
     * Clears all loaded chunks from the manager.
     */
    public void clearChunks() {
        chunks.clear();
        
        System.out.println("WorldManager: Cleared all chunks.");
    }

    /**
     * Gets the tile at the specified world coordinates.
     * @param worldX The world x-coordinate.
     * @param worldY The world y-coordinate.
     * @return The Tile at the given coordinates, or null if the chunk or tile doesn't exist.
     */
    public Tile getTileAt(int worldX, int worldY) {
        int chunkX = Math.floorDiv(worldX, Chunk.CHUNK_SIZE * TILE_SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.CHUNK_SIZE * TILE_SIZE);

        Chunk chunk = getChunk(chunkX, chunkY);

        int localX = Math.floorMod(worldX / TILE_SIZE, Chunk.CHUNK_SIZE);
        int localY = Math.floorMod(worldY / TILE_SIZE, Chunk.CHUNK_SIZE);
        
        return chunk.getTile(localX, localY);
    }

}
