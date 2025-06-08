package com.tavuc.ui.lights;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/**
 * Simple light source that fades out over time.
 */
public class DynamicLight {
    private double x;
    private double y;
    private float radius;
    private float intensity;

    public DynamicLight(double x, double y, float radius, float intensity) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.intensity = intensity;
    }

    /**
     * Updates the light intensity.
     * @return true if the light has fully faded
     */
    public boolean update() {
        intensity -= 0.05f;
        return intensity <= 0f;
    }

    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        if (intensity <= 0f) return;
        float[] dist = {0f, 1f};
        Color[] cols = {
            new Color(255, 240, 200, (int) (intensity * 180)),
            new Color(255, 240, 200, 0)
        };
        RadialGradientPaint p = new RadialGradientPaint(
            new Point2D.Double(x - offsetX, y - offsetY), radius, dist, cols);
        g2d.setPaint(p);
        g2d.fillRect((int)(x - offsetX - radius), (int)(y - offsetY - radius), (int)(radius * 2), (int)(radius * 2));
    }
}
