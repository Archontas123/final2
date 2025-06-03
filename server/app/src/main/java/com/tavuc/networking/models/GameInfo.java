package com.tavuc.networking.models;

public class GameInfo {
    public String gameId;
    public String planetName;
    public int playerCount;

    public GameInfo() {
    }

    public GameInfo(String gameId, String planetName, int playerCount) {
        this.gameId = gameId;
        this.planetName = planetName;
        this.playerCount = playerCount;
    }
}
