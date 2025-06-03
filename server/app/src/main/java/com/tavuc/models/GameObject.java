package com.tavuc.models;

import javax.swing.JComponent;
import java.awt.Rectangle;

public abstract class GameObject extends JComponent {
    
    private int x;
    private int y;
    private Rectangle hitbox;

    /**
     * Constructor for GameObject
     * @param xPosition The x position of the object.
     * @param yPosition The y position of the object.
     * @param width The width of the object.
     * @param height The height of the object.
     */
    public GameObject(int xPosition, int yPosition, int width, int height) {
        this.setSize(width, height);
        this.x = xPosition;
        this.y = yPosition;
        this.setLocation(xPosition, yPosition);
        this.hitbox = new Rectangle(xPosition, yPosition, width, height); 

    }

    /**
     * Updates the state of the object.
     */
    public abstract void update();

    /**
     * Checks if this object has collided with another object.
     * @param other The other object to check for collision with.
     * @return true if a collision has occurred, false otherwise.
     */
    public boolean hasCollidedWith(GameObject other) {
        return false;
    }

    /**
     * Handles the collision with another object.
     * @param other The other object that this object has collided with.
     */
    public void collided(GameObject other) {
        //TODO: ADD COLLISION DETECTION
    }

    /**
     * Gets the x position of the object.
     * @return The x position of the object.
     */
    public int getX() {
        return x;
    }


    /**
     * Gets the y position of the object.
     * @return The y position of the object.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the x field of the object.
     * @param x The new x field of the object.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y field of the object.
     * @param y The new y field of the object.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Sets the x position of the object + hitbox.
     * @param x The new x position of the object.
     */
    public void setXPosition(int x) {
        this.x = x;
        this.setLocation(x, y);
        this.hitbox.setLocation(x, y);
    }

    
    /**
     * Sets the y position of the object + hitbox.
     * @param y The new x position of the object.
     */
    public void setYPosition(int y) {
        this.y = y;
        this.setLocation(x, y);
        this.hitbox.setLocation(x, y);
    }


    /**
     * Sets the position of the object + hitbox.
     * @param x The new x position of the object.
     * @param y The new y position of the object.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.setLocation(x, y);
        this.hitbox.setLocation(x, y);
    }

    /**
     * Gets the hitbox of the object.
     * @return The hitbox of the object.
     */
    public Rectangle getHitBox() {
        return this.hitbox.getBounds();
    }
    
}
