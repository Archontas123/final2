package com.tavuc.managers;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.tavuc.models.entities.Player; // Added import
import com.tavuc.Client;
import com.tavuc.networking.models.PlayerJoinedBroadcast; // Added import
import com.tavuc.networking.models.PlayerMovedBroadcast; // Added import
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.ColorType;
import com.tavuc.models.planets.Tile;
import com.tavuc.networking.models.RequestChunkResponse;
import com.tavuc.networking.models.TileData;

/**
 * Manages the state of a single game world (e.g., a planet's surface) on the client side.
 */
public class WorldManager {
    
    /**
     * A map storing the loaded chunks of the world, keyed by their chunk coordinates.
     */
    private Map<Point, Chunk> chunks;
    /**
     * The color palette used for rendering the tiles in this world.
     */
    private ColorPallete palette;
    /**
     * The unique identifier for the game world this manager is responsible for.
     */
    private int gameId; 
    /**
     * The size of a single tile in pixels.
     */
    public static final int TILE_SIZE = 32; 
    /**
     * The radius of chunks to load around the player's current chunk.
     */
    private static final int chunkLoadRadius = 2; 
    /**
     * A shared Gson instance for JSON operations.
     */
    private static final Gson gson = new Gson();
    /**
     * A map storing other players currently in the world, keyed by their player ID.
     */
    private final Map<Integer, Player> otherPlayers = new ConcurrentHashMap<>(); // Added for other players

    /**
     * Constructs a new WorldManager for a specific game world.
     * @param gameId The unique identifier of the world to be managed.
     */
    public WorldManager(int gameId) {
        this.gameId = gameId;
        this.chunks = new ConcurrentHashMap<>();
    }


    /**
     * Gets a list of all other players currently in this world.
     * @return A new list containing all {@link Player} objects for other players.
     */
    public List<Player> getOtherPlayers() {
        return new ArrayList<>(otherPlayers.values());
    }

    /**
     * Retrieves a specific other player by their unique ID.
     * @param id The ID of the player to retrieve.
     * @return The {@link Player} object for the given ID, or {@code null} if not found.
     */
    public Player getOtherPlayer(int id) {
        return otherPlayers.get(id);
    }

    /**
     * Adds a new player to the world based on a broadcast event from the server.
     * @param event The {@link PlayerJoinedBroadcast} event containing the new player's data.
     */
    public void addPlayer(PlayerJoinedBroadcast event) {
        System.out.println("WorldManager.addPlayer: Received event for player ID: " + event.playerId + ", username: " + event.username + ". Current client player ID: " + Client.getInstance().getPlayerId());
        if (event.playerId.equals(String.valueOf(Client.getInstance().getPlayerId()))) {
            System.out.println("WorldManager.addPlayer: Skipping addPlayer for self (ID: " + event.playerId + ")");
            return; 
        }
        try {
            int pId = Integer.parseInt(event.playerId);
            System.out.println("WorldManager.addPlayer: Attempting to add player " + event.username + " (ID: " + pId + ") to otherPlayers.");
            Player player = new Player(pId, event.username);
            player.setX(event.x);
            player.setY(event.y);
            player.setDx(event.dx);
            player.setDy(event.dy);
            player.setDirection(event.directionAngle);
            otherPlayers.put(pId, player);
            System.out.println("WorldManager.addPlayer: Successfully added player " + event.username + " (ID: " + pId + ") to otherPlayers. otherPlayers size: " + otherPlayers.size());
            if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
        } catch (NumberFormatException e) {
            System.err.println("WorldManager: Error parsing playerId for addPlayer: " + event.playerId);
        }
    }

    /**
     * Updates the state (position, velocity, direction) of an existing player in the world
     * based on a broadcast event from the server.
     * @param event The {@link PlayerMovedBroadcast} event containing the updated player data.
     */
    public void updatePlayer(PlayerMovedBroadcast event) {
        if (event.playerId.equals(String.valueOf(Client.getInstance().getPlayerId()))) {
            return; 
        }
        try {
            int pId = Integer.parseInt(event.playerId);
            Player player = otherPlayers.get(pId);
            if (player != null) {
                player.setX(event.x);
                player.setY(event.y);
                player.setDx(event.dx);
                player.setDy(event.dy);
                player.setDirection(event.directionAngle);
                if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
            }
        } catch (NumberFormatException e) {
            System.err.println("WorldManager: Error parsing playerId for updatePlayer: " + event.playerId);
        }
    }

    /**
     * Removes a player from the world, typically when they disconnect or leave the area.
     * @param playerId The string representation of the ID of the player to remove.
     */
    public void removePlayer(String playerId) {
        int pId = Integer.parseInt(playerId);
        if (otherPlayers.remove(pId) != null) {
            if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
        }

    }

    /**
     * Sets the game ID for this world.
     * @param gameId The unique identifier for the world.
     */
    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the game ID for this world.
     * @return The unique identifier for the world.
     */
    public int getGameId() {
        return this.gameId;
    }

    /**
     * Sets the color palette for this world.
     * @param palette The {@link ColorPallete} to use for rendering.
     */
    public void setPallete(ColorPallete palette) {
        this.palette = palette;
    }

    /**
     * Gets the current color palette for this world.
     * @return The current {@link ColorPallete}.
     */
    public ColorPallete getCurrentPalette() {
        return palette;
    }

    /**
     * Gets the map of all currently loaded chunks.
     * @return A map of {@link Point} to {@link Chunk}.
     */
    public Map<Point, Chunk> getChunks() {
        return chunks;
    }

    /**
     * Retrieves a specific chunk by its chunk coordinates.
     * @param x The x-coordinate of the chunk.
     * @param y The y-coordinate of the chunk.
     * @return The {@link Chunk} at the specified coordinates, or {@code null} if not loaded.
     */
    public Chunk getChunk(int x, int y) {
        return chunks.get(new Point(x, y));
    }

    /**
     * Processes chunk data received from the server. It populates a local {@link Chunk}
     * object with tile data. 
     * @param response The {@link RequestChunkResponse} from the server containing tile data.
     */
    public void processChunkData(RequestChunkResponse response) {
        if (response == null) {
            System.err.println("WorldManager: Received null RequestChunkResponse.");
            return;
        }

        int chunkX = response.chunkX;
        int chunkY = response.chunkY;
        Chunk chunk = chunks.computeIfAbsent(new Point(chunkX, chunkY), k -> new Chunk(chunkX, chunkY));

        if (response.tiles == null) {
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    int globalX = chunkX * Chunk.CHUNK_SIZE + x;
                    int globalY = chunkY * Chunk.CHUNK_SIZE + y;
                    String tileType = (x + y) % 2 == 0 ? "GRASS" : "DIRT";
                    ColorType colorType = (x + y) % 2 == 0 ? ColorType.PRIMARY_SURFACE : ColorType.SECONDARY_SURFACE;
                    if (Math.random() < 0.1) { 
                        colorType = ColorType.PRIMARY_LIQUID;
                    }
                    Tile clientTile = new Tile(globalX, globalY, tileType, colorType);
                    chunk.setTile(x, y, clientTile);
                }
            }
        } else {
            try {
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
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {

            }
        }
        if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
    }

    /**
     * Determines which chunks should be visible based on the player's position and
     * requests data for any missing chunks from the server.
     * @param playerX The player's world x-coordinate in pixels.
     * @param playerY The player's world y-coordinate in pixels.
     * @param viewWidth The width of the viewport in pixels.
     * @param viewHeight The height of the viewport in pixels.
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
                    }
                }
            }
        }
    }

    /**
     * Clears all loaded chunks and other player data from the manager. 
     */
    public void clearChunks() {
        chunks.clear();
        otherPlayers.clear();
    }

    /**
     * Retrieves the tile at a specific world pixel coordinate.
     * @param worldX The absolute x-coordinate in the world (in pixels).
     * @param worldY The absolute y-coordinate in the world (in pixels).
     * @return The {@link Tile} at that location, or {@code null} if the chunk is not loaded.
     */
    public Tile getTileAt(int worldX, int worldY) {
        int chunkX = Math.floorDiv(worldX, Chunk.CHUNK_SIZE * TILE_SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.CHUNK_SIZE * TILE_SIZE);

        Chunk chunk = getChunk(chunkX, chunkY);
        if (chunk == null) return null;

        int localX = Math.floorMod(worldX / TILE_SIZE, Chunk.CHUNK_SIZE);
        int localY = Math.floorMod(worldY / TILE_SIZE, Chunk.CHUNK_SIZE);
        
        return chunk.getTile(localX, localY);
    }
}