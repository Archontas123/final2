package com.tavuc.networking.models;
/**
 * Represents the RequestChunkRequest networking message.
 */

public class RequestChunkRequest extends BaseMessage {
    public String gameId;
    public int chunkX;
    public int chunkY;

    /**
     * Constructs a new RequestChunkRequest.
     */
    public RequestChunkRequest(String gameId, int chunkX, int chunkY) {
        this.type = "REQUEST_CHUNK_REQUEST";
        this.gameId = gameId;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }
}
