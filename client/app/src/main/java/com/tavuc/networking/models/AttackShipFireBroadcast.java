package com.tavuc.networking.models;

public class AttackShipFireBroadcast extends BaseMessage {
    public String attackerId;
    public String targetPlayerId;
    public int fromX;
    public int fromY;
    public int toX;
    public int toY;

    public AttackShipFireBroadcast() {
        super(); 
    }
}
