package com.tavuc.networking.models;

import java.util.List;

public class GetPlayersResponse extends BaseMessage {
    public List<PlayerInitialData> players;

    public GetPlayersResponse() {
    }

    public GetPlayersResponse(List<PlayerInitialData> players) {
        this.type = "GET_PLAYERS_RESPONSE";
        this.players = players;
    }
}
