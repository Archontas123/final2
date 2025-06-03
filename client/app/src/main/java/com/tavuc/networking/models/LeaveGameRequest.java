package com.tavuc.networking.models;

public class LeaveGameRequest extends BaseMessage {
    public LeaveGameRequest() {
        this.type = "LEAVE_GAME_REQUEST";
    }
}
