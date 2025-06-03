package com.tavuc.networking.models;

public class RequestPaletteRequest extends BaseMessage {
    public String gameId;

    public RequestPaletteRequest(String gameId) {
        this.type = "REQUEST_PALETTE_REQUEST";
        this.gameId = gameId;
    }
}
