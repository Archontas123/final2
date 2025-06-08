package com.tavuc.ui.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** Expanding ring when using a force ability. */
public class ForceEffectParticle extends Particle {
    private double size = 10;
    private float alpha = 1f;

    public ForceEffectParticle(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.life = 0.5;
    }

    @Override
    public boolean update() {
        boolean expired = super.update();
        alpha = (float) Math.max(0, life / 0.5);
        size += 1.0;
        return expired;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.setColor(new Color(100, 140, 255, (int) (alpha * 150)));
        g2d.draw(new Ellipse2D.Double(x - offsetX - size / 2, y - offsetY - size / 2, size, size));
    }
}
