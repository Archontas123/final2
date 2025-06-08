package com.tavuc.networking.models;

public class ExtractionRequest extends BaseMessage {
    public String playerId;

    public ExtractionRequest() {
        this.type = "EXTRACTION_REQUEST";
    }

    public ExtractionRequest(String playerId) {
        this();
        this.playerId = playerId;
    }
}
