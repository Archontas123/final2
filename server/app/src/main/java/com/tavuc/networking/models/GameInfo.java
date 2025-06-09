package com.tavuc.networking.models;
/**
 * Represents the GameInfo networking message.
 */

public class GameInfo {
    public String gameId;
    public String planetName;
    public int playerCount;

    /**
     * Constructs a new GameInfo.
     */
    public GameInfo() {
    }

    /**
     * Constructs a new GameInfo.
     */
    public GameInfo(String gameId, String planetName, int playerCount) {
        this.gameId = gameId;
        this.planetName = planetName;
        this.playerCount = playerCount;
    }
}
