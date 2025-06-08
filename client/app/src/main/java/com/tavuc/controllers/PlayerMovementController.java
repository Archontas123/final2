package com.tavuc.controllers;

import com.tavuc.utils.Vector2D;
import com.tavuc.Client;

/**
 * Handles advanced player movement features such as momentum, sliding and dodging.
 */
public class PlayerMovementController {
    private final Vector2D velocity = new Vector2D();
    private final Vector2D acceleration = new Vector2D();
    private double friction = 0.85;
    private double maxSpeed = 5.0;
    private final double strafeModifier = 1.1;
    private MovementState currentState = MovementState.IDLE;
    private boolean isSliding = false;
    private double slideTimer = 0.0;
    private final Vector2D lastInputDirection = new Vector2D();

    /**
     * Updates the movement each frame.
     * @param deltaTime elapsed time in seconds
     * @param frictionValue friction coefficient based on the current surface
     */
    public void update(double deltaTime, double frictionValue) {
        this.friction = frictionValue;
        // Simple momentum based movement
        velocity.add(new Vector2D(acceleration.getX() * deltaTime, acceleration.getY() * deltaTime));
        if (velocity.length() > maxSpeed) {
            velocity.normalize();
            velocity.scale(maxSpeed);
        }

        // Apply friction when no acceleration
        if (acceleration.length() == 0) {
            velocity.scale(friction);
            if (velocity.length() < 0.01) {
                velocity.set(0, 0);
            }
        }

        // Update slide timer
        if (isSliding) {
            slideTimer -= deltaTime;
            if (slideTimer <= 0) {
                isSliding = false;
            }
        }

        // Update state
        if (isSliding) {
            currentState = MovementState.SLIDING;
        } else if (velocity.length() > 0.1) {
            currentState = MovementState.RUNNING;
        } else {
            currentState = MovementState.IDLE;
        }
    }

    /**
     * Sets the desired movement direction. Magnitude is ignored and only the direction is used.
     */
    public void setInputDirection(double x, double y) {
        lastInputDirection.set(x, y);
        acceleration.set(x, y);
        if (acceleration.length() > 1) {
            acceleration.normalize();
        }
        if (Math.abs(x) > 0 && Math.abs(y) > 0) {
            acceleration.scale(strafeModifier);
        }
    }

    /** Start a slide action in the current input direction. */
    public void startSlide() {
        if (!isSliding) {
            isSliding = true;
            slideTimer = 0.3; // duration of slide in seconds
            velocity.set(lastInputDirection.getX() * maxSpeed * 1.2, lastInputDirection.getY() * maxSpeed * 1.2);
            if (Client.currentGamePanel != null) {
                Client.currentGamePanel.triggerScreenShake(4, 3);
            }
        }
    }

    /** Instant dodge in the input direction. */
    public void dodge() {
        velocity.set(lastInputDirection.getX() * maxSpeed * 1.5, lastInputDirection.getY() * maxSpeed * 1.5);
        currentState = MovementState.DODGING;
        if (Client.currentGamePanel != null) {
            Client.currentGamePanel.triggerScreenShake(6, 5);
        }
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public MovementState getCurrentState() {
        return currentState;
    }

    /** Predict position delta for network smoothing. */
    public Vector2D predictNextDelta(double deltaTime) {
        Vector2D vel = new Vector2D(velocity.getX(), velocity.getY());
        vel.add(new Vector2D(acceleration.getX() * deltaTime, acceleration.getY() * deltaTime));
        if (vel.length() > maxSpeed) {
            vel.normalize();
            vel.scale(maxSpeed);
        }
        return new Vector2D(vel.getX() * deltaTime, vel.getY() * deltaTime);
    }
}
