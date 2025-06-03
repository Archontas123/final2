package com.tavuc.networking.models;

public class LoginResponse extends BaseMessage {
    public boolean success;
    public String message;
    public String playerId;
    public String username;

    public LoginResponse(boolean success, String message, String playerId, String username) {
        this.type = "LOGIN_RESPONSE";
        this.success = success;
        this.message = message;
        this.playerId = playerId;
        this.username = username;
    }
}
