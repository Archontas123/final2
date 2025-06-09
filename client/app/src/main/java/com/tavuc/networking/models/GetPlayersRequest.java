package com.tavuc.networking.models;
/**
 * Represents the GetPlayersRequest networking message.
 */

public class GetPlayersRequest extends BaseMessage {
    public String gameId;

    public GetPlayersRequest(String gameId) {
        this.type = "GET_PLAYERS_REQUEST";
        this.gameId = gameId;
    }
}
