package com.tavuc.models.space;


public class PlayerShip extends BaseShip {

    private static final float PLAYER_MAX_HEALTH = 200f;
    private static final float PLAYER_MAX_SPEED = 400f;
    private static final float PLAYER_ACCELERATION = 200f;
    private static final float PLAYER_TURN_RATE = 3.0f; 
    private static final float PLAYER_FIRE_RATE = 3.0f; 
    private static final float PLAYER_PROJECTILE_DAMAGE = 15.0f;
    private String playerId; 

    public PlayerShip(String entityId, String playerId, int x, int y, int width, int height) {
        super(entityId, x, y, width, height,
              PLAYER_MAX_HEALTH,
              PLAYER_MAX_SPEED,
              PLAYER_ACCELERATION,
              PLAYER_TURN_RATE,
              PLAYER_FIRE_RATE,
              PLAYER_PROJECTILE_DAMAGE);
        this.playerId = playerId;
    }

    @Override
    public void update() {
        float timeStep = 1.0f / 60.0f; 
        
        int newX = getX() + (int)(velocityX * timeStep);
        int newY = getY() + (int)(velocityY * timeStep);
        setPosition(newX, newY);
    }

   
    public String getPlayerId() {
        return playerId;
    }

}
