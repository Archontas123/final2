package com.tavuc.networking.models;

public class JoinGameRequest extends BaseMessage {
    public String gameId;

    public JoinGameRequest(String gameId) {
        this.type = "JOIN_GAME_REQUEST";
        this.gameId = gameId;
    }
}
