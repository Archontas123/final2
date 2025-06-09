package com.tavuc.networking.models;
/**
 * Represents the LoginResponse networking message.
 */

public class LoginResponse extends BaseMessage {
    public boolean success;
    public String message;
    public String playerId;
    public String username;

    /**
     * Constructs a new LoginResponse.
     */
    public LoginResponse() {
    }

    /**
     * Constructs a new LoginResponse.
     */
    public LoginResponse(boolean success, String message, String playerId, String username) {
        this.type = "LOGIN_RESPONSE";
        this.success = success;
        this.message = message;
        this.playerId = playerId;
        this.username = username;
    }
}
