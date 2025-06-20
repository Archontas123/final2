package com.tavuc.managers;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.tavuc.models.entities.Dummy;
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

public class WorldManager {
    
    private Map<Point, Chunk> chunks;
    private ColorPallete palette;
    private int gameId; 
    public static final int TILE_SIZE = 32; 
    private static final int chunkLoadRadius = 2; 
    private static final Gson gson = new Gson();
    private final Map<Integer, Dummy> dummies = new ConcurrentHashMap<>();
    private final Map<Integer, Player> otherPlayers = new ConcurrentHashMap<>(); // Added for other players

    public WorldManager(int gameId) {
        this.gameId = gameId;
        this.chunks = new ConcurrentHashMap<>();
    }

    public void updateDummy(int id, float x, float y) {
        Dummy dummy = dummies.get(id);
        if (dummy == null) {
            dummy = new Dummy(id, x, y);
            dummies.put(id, dummy);
        } else {
            dummy.setX(x);
            dummy.setY(y);
        }
        if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
    }

    public void removeDummy(int id) {
        if (dummies.remove(id) != null) {
            if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
        }
    }

    public List<Dummy> getDummies() {
        return new ArrayList<>(dummies.values());
    }

    public List<Player> getOtherPlayers() {
        return new ArrayList<>(otherPlayers.values());
    }

    public void addPlayer(PlayerJoinedBroadcast event) {
        System.out.println("WorldManager.addPlayer: Received event for player ID: " + event.playerId + ", username: " + event.username + ". Current client player ID: " + Client.getInstance().getPlayerId());
        if (event.playerId.equals(String.valueOf(Client.getInstance().getPlayerId()))) {
            System.out.println("WorldManager.addPlayer: Skipping addPlayer for self (ID: " + event.playerId + ")");
            return; // Don't add self
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

    public void updatePlayer(PlayerMovedBroadcast event) {
        if (event.playerId.equals(String.valueOf(Client.getInstance().getPlayerId()))) {
            return; // Don't update self from broadcast
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
                // player.update(); // Let GamePanel's loop call update if needed, or call here if direct update is desired
                if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
            }
        } catch (NumberFormatException e) {
            System.err.println("WorldManager: Error parsing playerId for updatePlayer: " + event.playerId);
        }
    }

    public void removePlayer(String playerId) {
        try {
            int pId = Integer.parseInt(playerId);
            if (otherPlayers.remove(pId) != null) {
                if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
            }
        } catch (NumberFormatException e) {
            System.err.println("WorldManager: Error parsing playerId for removePlayer: " + playerId);
        }
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return this.gameId;
    }

    public void setPallete(ColorPallete palette) {
        this.palette = palette;
    }

    public ColorPallete getCurrentPalette() {
        return palette;
    }

    public Map<Point, Chunk> getChunks() {
        return chunks;
    }

    public Chunk getChunk(int x, int y) {
        return chunks.get(new Point(x, y));
    }

    public void processChunkData(RequestChunkResponse response) {
        if (response == null) {
            System.err.println("WorldManager: Received null RequestChunkResponse.");
            return;
        }

        int chunkX = response.chunkX;
        int chunkY = response.chunkY;
        Chunk chunk = chunks.computeIfAbsent(new Point(chunkX, chunkY), k -> new Chunk(chunkX, chunkY));

        if (response.tiles == null) {
            // Offline mode or empty chunk from server: Procedurally generate the chunk
            System.out.println("WorldManager: No tile data for chunk " + chunkX + "," + chunkY + ". Generating procedurally.");
            for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    int globalX = chunkX * Chunk.CHUNK_SIZE + x;
                    int globalY = chunkY * Chunk.CHUNK_SIZE + y;
                    // Simple procedural generation: alternate between two tile types
                    String tileType = (x + y) % 2 == 0 ? "GRASS" : "DIRT";
                    ColorType colorType = (x + y) % 2 == 0 ? ColorType.PRIMARY_SURFACE : ColorType.SECONDARY_SURFACE;
                    if (Math.random() < 0.1) { // Add some water
                        tileType = "WATER";
                        colorType = ColorType.PRIMARY_LIQUID;
                    }
                    Tile clientTile = new Tile(globalX, globalY, tileType, colorType);
                    chunk.setTile(x, y, clientTile);
                }
            }
        } else {
            // Process tiles from server response
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
                        // System.err.println("WorldManager: Error parsing ColorType from TileData: " + tileData.colorTypeName + " - " + e.getMessage());
                    } catch (Exception e) {
                        // System.err.println("WorldManager: Error processing individual tile data: " + gson.toJson(tileData) + " - " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // System.err.println("WorldManager: Error processing chunk data response: " + gson.toJson(response) + " - " + e.getMessage());
                // e.printStackTrace();
            }
        }
        if (Client.currentGamePanel != null) Client.currentGamePanel.repaint();
    }

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
                        // System.err.println("WorldManager: Error requesting chunk " + cx + "," + cy + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    public void clearChunks() {
        chunks.clear();
        dummies.clear();
        otherPlayers.clear();
    }

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
