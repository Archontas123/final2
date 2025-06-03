package com.tavuc.models;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.JComponent;

public abstract class GameObject extends JComponent {
    
    protected double x, y;
    protected int width, height;
    protected Rectangle hitbox;

    /**
     * Constructor for GameObject
     * @param x The x position of the object
     * @param y The y position of the object
     * @param width The width of the object
     * @param height The height of the object
     */
    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle((int) x, (int) y, width, height);
    }

    /**
     * Gets the x position of the object.
     * @return the x position
     */
    @Override
    public int getX() {
        return (int) x;
    }

    /**
     * Sets the x position of the object and updates the hitbox.
     * @param x the new x position
     */
    public void setX(double x) {
        this.x = x;
        updateHitbox();
    }

    /**
     * Gets the y position of the object.
     * @return the y position
     */
    @Override
    public int getY() {
        return (int) y;
    }

    /**
     * Sets the y position of the object and updates the hitbox.
     * @param y the new y position
     */
    public void setY(double y) {
        this.y = y;
        updateHitbox();
    }

    /**
     * Gets the width of the object.
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the object and updates the hitbox.
     * @param width the new width
     */
    public void setWidth(int width) {
        this.width = width;
        updateHitbox();
    }

    /**
     * Gets the height of the object.
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the object and updates the hitbox.
     * @param height the new height
     */
    public void setHeight(int height) {
        this.height = height;
        updateHitbox();
    }

    /**
     * Gets the hitbox of the object.
     * @return the hitbox as a Rectangle
     */
    public Rectangle getHitbox() {
        return hitbox;
    }

    /**
     * Updates the hitbox based on the current position and dimensions of the object.
     */
    protected void updateHitbox() {
        hitbox.x = (int) x;
        hitbox.y = (int) y;
        hitbox.width = width;
        hitbox.height = height;
    }

    /**
     * Draws the object on the provided Graphics2D context.
     * @param g2d the Graphics2D context to draw on
     * @param offsetX the x offset for drawing
     * @param offsetY the y offset for drawing
     */
    public abstract void draw(Graphics2D g2d, double offsetX, double offsetY);
}
