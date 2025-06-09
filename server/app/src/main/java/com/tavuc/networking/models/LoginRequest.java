package com.tavuc.networking.models;
/**
 * Represents the LoginRequest networking message.
 */

public class LoginRequest extends BaseMessage {
    public String username;
    public String password;

    public LoginRequest(String username, String password) {
        this.type = "LOGIN_REQUEST";
        this.username = username;
        this.password = password;
    }
}
