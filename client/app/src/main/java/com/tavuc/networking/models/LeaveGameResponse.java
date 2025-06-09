package com.tavuc.networking.models;
/**
 * Represents the LeaveGameResponse networking message.
 */

public class LeaveGameResponse extends BaseMessage {
    public boolean success;
    public String message;
    public String gameId;

    /**
     * Constructs a new LeaveGameResponse.
     */
    public LeaveGameResponse() {
    }

    /**
     * Constructs a new LeaveGameResponse.
     */
    public LeaveGameResponse(boolean success, String message, String gameId) {
        this.type = "LEAVE_GAME_RESPONSE";
        this.success = success;
        this.message = message;
        this.gameId = gameId;
    }
}
