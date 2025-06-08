package com.tavuc.ui.effects;

import java.awt.Graphics2D;

/** Simple particle used for movement effects. */
public abstract class MovementParticle {
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
