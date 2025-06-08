package com.tavuc.ui.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** Quick flash emitted when a blaster is fired. */
public class MuzzleFlashParticle extends Particle {
    private double size = 8;
    private float alpha = 1f;

    public MuzzleFlashParticle(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.life = 0.1;
    }

    @Override
    public boolean update() {
        boolean expired = super.update();
        alpha = (float)Math.max(0, life / 0.1);
        size += 0.3;
        return expired;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.setColor(new Color(255, 220, 120, (int)(alpha * 255)));
        g2d.fill(new Ellipse2D.Double(x - offsetX - size / 2, y - offsetY - size / 2, size, size));
    }
}
