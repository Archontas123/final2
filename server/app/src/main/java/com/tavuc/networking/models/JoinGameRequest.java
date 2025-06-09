package com.tavuc.networking.models;
/**
 * Represents the JoinGameRequest networking message.
 */

public class JoinGameRequest extends BaseMessage {
    public String gameId;

    /**
     * Constructs a new JoinGameRequest.
     */
    public JoinGameRequest(String gameId) {
        this.type = "JOIN_GAME_REQUEST";
        this.gameId = gameId;
    }
}
