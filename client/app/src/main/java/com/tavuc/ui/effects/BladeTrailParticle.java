package com.tavuc.ui.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;

/** Simple arc used for lightsaber swings. */
public class BladeTrailParticle extends Particle {
    private double size = 12;
    private float alpha = 1f;

    public BladeTrailParticle(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.life = 0.2;
    }

    @Override
    public boolean update() {
        boolean expired = super.update();
        alpha = (float) Math.max(0, life / 0.2);
        size += 0.6;
        return expired;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.setColor(new Color(255, 255, 255, (int) (alpha * 180)));
        double d = size * 2;
        g2d.draw(new Arc2D.Double(x - offsetX - size, y - offsetY - size, d, d, 0, 180, Arc2D.OPEN));
    }
}
