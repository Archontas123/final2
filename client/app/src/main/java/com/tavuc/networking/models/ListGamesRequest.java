package com.tavuc.networking.models;
/**
 * Represents the ListGamesRequest networking message.
 */

public class ListGamesRequest extends BaseMessage {
    /**
     * Constructs a new ListGamesRequest.
     */
    public ListGamesRequest() {
        this.type = "LIST_GAMES_REQUEST";
    }
}
