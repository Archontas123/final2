package com.tavuc.ui.components;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Represents a short-lived, animated text popup.
 */
public class DamagePopup {
    /** The current x-coordinate of the popup in the world. */
    private double x;
    /** The current y-coordinate of the popup in the world. */
    private double y;
    /** The text content to be displayed. */
    private final String text;
    /** The color of the text. */
    private final Color color;
    /** The current alpha (transparency) of the text, used for fading out. */
    private float alpha = 1f;

    /** The total duration of the popup's animation, in frames. */
    private static final int DURATION_FRAMES = 60;
    /** The speed at which the popup rises, in pixels per frame. */
    private static final double RISE_SPEED = 0.5;

    /**
     * Constructs a new DamagePopup.
     * @param x The initial x-coordinate in the world where the popup should appear.
     * @param y The initial y-coordinate in the world where the popup should appear.
     * @param text The text to display (e.g., a damage number).
     * @param color The color of the text.
     */
    public DamagePopup(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
    }

    /**
     * Updates the state of the popup for a single frame. 
     * @return {@code true} if the popup has completely faded and should be removed
     *         from the game; {@code false} otherwise.
     */
    public boolean update() {
        y -= RISE_SPEED;
        alpha -= 1f / DURATION_FRAMES;
        return alpha <= 0f;
    }

    /**
     * Renders the damage popup text on the screen at its current position and
     * with its current transparency.
     * @param g2d The {@link Graphics2D} context to draw on.
     * @param offsetX The horizontal offset of the camera or viewport.
     * @param offsetY The vertical offset of the camera or viewport.
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