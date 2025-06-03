package com.tavuc.ui.panels;

public interface ISpacePanel {
    void updateOtherShip(int otherPlayerId, double x, double y, double angle, double dx, double dy, boolean thrusting);
    void removeOtherShip(int otherPlayerId);
    
    void spawnCruiser(String entityId, int x, int y, float health, float maxHealth);
    void spawnAttackShip(String entityId, int x, int y, String parentId, int targetPlayerId);
    void updateCruiser(String entityId, int x, int y, float orientation, float health, float maxHealth, String aiState);
    void updateAttackShip(String entityId, int x, int y, float orientation, float health, String aiState);
    void showAttackShipFire(String entityId, int targetPlayerId, int fromX, int fromY, int toX, int toY);
    void removeCruiser(String entityId);
    void removeAttackShip(String entityId);
    void spawnProjectile(String projectileId, int x, int y, int width, int height, float orientation, float speed, float damage, String firedBy);
}
