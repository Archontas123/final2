package com.tavuc.models.entities;

// Assuming a base Entity class or GameObject class exists on the client-side.
// If not, this might need to extend a more generic client-side model object.
// For now, let's make it a simple class.
public class Dummy { // If there's a client-side Entity or GameObject, it should extend that.

    private int id;
    private float x, y;
    // Client-side dummies might not need health, speed, etc., if only for display
    // and AI is server-authoritative.

    public Dummy(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    // Client-side update might be for animations or local effects if any
    public void update() {
        // Placeholder for client-side specific update logic
    }

    // Client-side render method will be important
    public void render(java.awt.Graphics g) {
        // Placeholder for rendering logic
        // Example: g.fillRect((int)x, (int)y, 32, 32); // Draw a simple square
    }
}
