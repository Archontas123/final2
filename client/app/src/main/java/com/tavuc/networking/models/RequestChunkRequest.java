package com.tavuc.networking.models;

public class RequestChunkRequest extends BaseMessage {
    public String gameId;
    public int chunkX;
    public int chunkY;

    public RequestChunkRequest(String gameId, int chunkX, int chunkY) {
        this.type = "REQUEST_CHUNK_REQUEST";
        this.gameId = gameId;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }
}
