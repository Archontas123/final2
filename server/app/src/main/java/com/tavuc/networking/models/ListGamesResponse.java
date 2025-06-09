package com.tavuc.networking.models;
/**
 * Represents the ListGamesResponse networking message.
 */

import java.util.List;

public class ListGamesResponse extends BaseMessage {
    public List<GameInfo> games;

    /**
     * Constructs a new ListGamesResponse.
     */
    public ListGamesResponse() {
    }

    /**
     * Constructs a new ListGamesResponse.
     */
    public ListGamesResponse(List<GameInfo> games) {
        this.type = "LIST_GAMES_RESPONSE";
        this.games = games;
    }
}
