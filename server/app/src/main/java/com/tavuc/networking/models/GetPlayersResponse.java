package com.tavuc.networking.models;
/**
 * Represents the GetPlayersResponse networking message.
 */

import java.util.List;

public class GetPlayersResponse extends BaseMessage {
    public List<PlayerInitialData> players;

    /**
     * Constructs a new GetPlayersResponse.
     */
    public GetPlayersResponse() {
    }

    /**
     * Constructs a new GetPlayersResponse.
     */
    public GetPlayersResponse(List<PlayerInitialData> players) {
        this.type = "GET_PLAYERS_RESPONSE";
        this.players = players;
    }
}
