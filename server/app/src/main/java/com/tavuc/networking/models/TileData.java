package com.tavuc.networking.models;
/**
 * Represents the TileData networking message.
 */

public class TileData {
    public int x;
    public int y;
    public String tileType;
    public String colorTypeName;

    public TileData() {
    }

    public TileData(int x, int y, String tileType, String colorTypeName) {
        this.x = x;
        this.y = y;
        this.tileType = tileType;
        this.colorTypeName = colorTypeName;
    }
}
