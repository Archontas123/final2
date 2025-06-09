package com.tavuc.networking.models;
/**
 * Represents the ListGamesRequest networking message.
 */

public class ListGamesRequest extends BaseMessage {
    public ListGamesRequest() {
        this.type = "LIST_GAMES_REQUEST";
    }
}
