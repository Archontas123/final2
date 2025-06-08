package com.tavuc.ui.effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/** Small dust puff spawned during sliding. */
public class DustParticle extends MovementParticle {
    private double size;
    private float alpha;

    public DustParticle(double x, double y) {
        this.x = x;
        this.y = y;
        this.vx = (Math.random() - 0.5) * 0.5;
        this.vy = -Math.random() * 0.5;
        this.life = 0.4 + Math.random() * 0.2;
        this.size = 4 + Math.random() * 3;
        this.alpha = 1f;
    }

    @Override
    public boolean update() {
        boolean expired = super.update();
        alpha = (float)Math.max(0, life / 0.4);
        return expired;
    }

    @Override
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        g2d.setColor(new Color(150, 150, 150, (int)(alpha * 180)));
        g2d.fill(new Ellipse2D.Double(x - offsetX, y - offsetY, size, size));
    }
}
