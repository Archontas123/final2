package com.tavuc.ui.panels;

public interface ISpacePanel {
    void updateOtherShip(int otherPlayerId, double x, double y, double angle, double dx, double dy, boolean thrusting);
    void removeOtherShip(int otherPlayerId);
}
