package com.tavuc.networking.models;
/**
 * Represents the RegisterRequest networking message.
 */

public class RegisterRequest extends BaseMessage {
    public String username;
    public String password;

    public RegisterRequest(String username, String password) {
        this.type = "REGISTER_REQUEST";
        this.username = username;
        this.password = password;
    }
}
