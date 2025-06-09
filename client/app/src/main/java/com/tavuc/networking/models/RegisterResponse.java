package com.tavuc.networking.models;
/**
 * Represents the RegisterResponse networking message.
 */

public class RegisterResponse extends BaseMessage {
    public boolean success;
    public String message;
    public String playerId;

    public RegisterResponse() {
    }

    public RegisterResponse(boolean success, String message, String playerId) {
        this.type = "REGISTER_RESPONSE";
        this.success = success;
        this.message = message;
        this.playerId = playerId;
    }
}
