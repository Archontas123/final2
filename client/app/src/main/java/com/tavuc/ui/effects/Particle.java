package com.tavuc.ui.effects;

import java.awt.Graphics2D;

/** Generic world particle with position, velocity and lifespan. */
public abstract class Particle {
    protected double x, y;
    protected double vx, vy;
    protected double life;

    /**
     * Updates the particle state.
     * @return true if the particle has expired
     */
    public boolean update() {
        x += vx;
        y += vy;
        life -= 0.016;
        return life <= 0;
    }

    public abstract void draw(Graphics2D g2d, double offsetX, double offsetY);
}
