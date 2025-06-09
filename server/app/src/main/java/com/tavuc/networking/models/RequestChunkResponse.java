package com.tavuc.networking.models;
/**
 * Represents the RequestChunkResponse networking message.
 */

import java.util.List;

public class RequestChunkResponse extends BaseMessage {
    public int chunkX;
    public int chunkY;
    public List<TileData> tiles;

    /**
     * Constructs a new RequestChunkResponse.
     */
    public RequestChunkResponse() {
    }

    /**
     * Constructs a new RequestChunkResponse.
     */
    public RequestChunkResponse(int chunkX, int chunkY, List<TileData> tiles) {
        this.type = "REQUEST_CHUNK_RESPONSE";
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.tiles = tiles;
    }
}
