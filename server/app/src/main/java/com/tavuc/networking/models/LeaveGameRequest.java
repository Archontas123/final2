package com.tavuc.networking.models;
/**
 * Represents the LeaveGameRequest networking message.
 */

public class LeaveGameRequest extends BaseMessage {
    public LeaveGameRequest() {
        this.type = "LEAVE_GAME_REQUEST";
    }
}
