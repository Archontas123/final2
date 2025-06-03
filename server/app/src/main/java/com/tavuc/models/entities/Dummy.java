package com.tavuc.models.entities;

public class Dummy extends Entity {

    private static final double DEFAULT_HEALTH = 100.0;
    private static final int DEFAULT_WIDTH = 32;
    private static final int DEFAULT_HEIGHT = 32;
    private static final float MOVEMENT_SPEED = 0.5f;

    public Dummy(int id, float x, float y) {
        super(id, "Dummy", (int)x, (int)y, DEFAULT_HEALTH, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    public void update() {
        super.update(); // Call super.update() to apply dx/dy changes

        double moveDirection = Math.random() * 2 * Math.PI; 

        float newDx = (float) (Math.cos(moveDirection) * MOVEMENT_SPEED);
        float newDy = (float) (Math.sin(moveDirection) * MOVEMENT_SPEED);

        setDx(newDx);
        setDy(newDy);
    }

    // Additional methods specific to Dummy AI can be added here
    // For example, detecting player, attacking, etc.
}
