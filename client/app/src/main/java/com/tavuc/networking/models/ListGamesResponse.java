package com.tavuc.networking.models;
/**
 * Represents the ListGamesResponse networking message.
 */

import java.util.List;

public class ListGamesResponse extends BaseMessage {
    public List<GameInfo> games;

    public ListGamesResponse() {
    }

    public ListGamesResponse(List<GameInfo> games) {
        this.type = "LIST_GAMES_RESPONSE";
        this.games = games;
    }
}
