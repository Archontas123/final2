package com.tavuc.networking.models;
/**
 * Represents the LeaveGameRequest networking message.
 */

public class LeaveGameRequest extends BaseMessage {
    /**
     * Constructs a new LeaveGameRequest.
     */
    public LeaveGameRequest() {
        this.type = "LEAVE_GAME_REQUEST";
    }
}
