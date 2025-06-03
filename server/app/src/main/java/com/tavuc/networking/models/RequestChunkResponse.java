package com.tavuc.networking.models;

import java.util.List;

public class RequestChunkResponse extends BaseMessage {
    public int chunkX;
    public int chunkY;
    public List<TileData> tiles;

    public RequestChunkResponse() {
    }

    public RequestChunkResponse(int chunkX, int chunkY, List<TileData> tiles) {
        this.type = "REQUEST_CHUNK_RESPONSE";
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.tiles = tiles;
    }
}
