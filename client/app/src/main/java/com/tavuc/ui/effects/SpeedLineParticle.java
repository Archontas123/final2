package com.tavuc.ui.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/** Streak line used when dodging. */
public class SpeedLineParticle extends MovementParticle {
    private double length;
    private float alpha = 1f;
    private double dir;

    public SpeedLineParticle(double x, double y, double dir) {
        this.x = x;
        this.y = y;
        this.dir = dir;
        this.vx = Math.cos(dir) * -2.0;
        this.vy = Math.sin(dir) * -2.0;
        this.life = 0.2;
        this.length = 12;
    }

    @Override
    public boolean update() {
        boolean expired = super.update();
        alpha = (float)Math.max(0, life / 0.2);
        return expired;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.setColor(new Color(200, 200, 255, (int)(alpha * 200)));
        double ex = x - offsetX + Math.cos(dir) * length;
        double ey = y - offsetY + Math.sin(dir) * length;
        g2d.draw(new Line2D.Double(x - offsetX, y - offsetY, ex, ey));
    }
}
