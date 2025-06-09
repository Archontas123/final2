package com.tavuc.networking.models;
/**
 * Represents the LeaveGameResponse networking message.
 */

public class LeaveGameResponse extends BaseMessage {
    public boolean success;
    public String message;
    public String gameId;

    public LeaveGameResponse() {
    }

    public LeaveGameResponse(boolean success, String message, String gameId) {
        this.type = "LEAVE_GAME_RESPONSE";
        this.success = success;
        this.message = message;
        this.gameId = gameId;
    }
}
