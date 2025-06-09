package com.tavuc.networking.models;
/**
 * Represents the JoinGameResponse networking message.
 */

import java.util.List;

public class JoinGameResponse extends BaseMessage {
    public boolean success;
    public String message;
    public String gameId;
    public String planetName;
    public List<PlayerInitialData> playersInGame;

    public JoinGameResponse() {
    }

    public JoinGameResponse(boolean success, String message, String gameId, String planetName, List<PlayerInitialData> playersInGame) {
        this.type = "JOIN_GAME_RESPONSE";
        this.success = success;
        this.message = message;
        this.gameId = gameId;
        this.planetName = planetName;
        this.playersInGame = playersInGame;
    }
}
