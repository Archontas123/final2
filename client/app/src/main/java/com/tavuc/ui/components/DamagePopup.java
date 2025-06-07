package com.tavuc.ui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Simple floating text effect used to display damage numbers.
 */
public class DamagePopup {
    private double x;
    private double y;
    private final String text;
    private final Color color;
    private float alpha = 1f;

    private static final int DURATION_FRAMES = 60;
    private static final double RISE_SPEED = 0.5;

    public DamagePopup(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
    }

    /**
     * Updates the popup position and alpha.
     * @return true if the popup has fully faded and should be removed
     */
    public boolean update() {
        y -= RISE_SPEED;
        alpha -= 1f / DURATION_FRAMES;
        return alpha <= 0f;
    }

    /**
     * Draws the popup relative to the given world offset.
     */
    public void draw(Graphics2D g2d, double offsetX, double offsetY) {
        AlphaComposite old = (AlphaComposite) g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
        g2d.setColor(color);
        Font oldFont = g2d.getFont();
        g2d.setFont(oldFont.deriveFont(Font.BOLD, 14f));
        g2d.drawString(text, (int) (x - offsetX), (int) (y - offsetY));
        g2d.setFont(oldFont);
        g2d.setComposite(old);
    }
}
