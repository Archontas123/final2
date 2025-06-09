package com.tavuc.networking.models;
/**
 * Represents the RequestPaletteRequest networking message.
 */

public class RequestPaletteRequest extends BaseMessage {
    public String gameId;

    /**
     * Constructs a new RequestPaletteRequest.
     */
    public RequestPaletteRequest(String gameId) {
        this.type = "REQUEST_PALETTE_REQUEST";
        this.gameId = gameId;
    }
}
