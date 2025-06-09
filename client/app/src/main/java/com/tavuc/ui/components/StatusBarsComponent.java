package com.tavuc.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.AlphaComposite;

/**
 * A custom UI component that displays health and shield status.
 */
public class StatusBarsComponent extends JPanel {
    /** The current health value, as a percentage (0-100). */
    private int health = 100; 
    /** The current shield value, as a percentage (0-100). */
    private int shield = 75;  

    /** The color for the main frame and border elements. */
    private static final Color FRAME_COLOR = new Color(180, 190, 210, 120);
    /** The color for the drop shadow behind the main panel. */
    private static final Color FRAME_SHADOW = new Color(10, 15, 25, 180);
    /** The color for text labels (not currently used). */
    private static final Color TEXT_COLOR = new Color(200, 210, 230, 200);
    
    /** The primary color for the health bar fill. */
    private static final Color HEALTH_COLOR = new Color(220, 60, 40);
    /** The color for the health bar's glow and highlight effects. */
    private static final Color HEALTH_GLOW = new Color(255, 100, 80);
    /** The dark, bottom color for the health bar's gradient. */
    private static final Color HEALTH_DARK = new Color(80, 20, 15);
    
    /** The primary color for the shield bar fill. */
    private static final Color SHIELD_COLOR = new Color(80, 160, 220);
    /** The color for the shield bar's glow and highlight effects. */
    private static final Color SHIELD_GLOW = new Color(120, 200, 255);
    /** The dark, bottom color for the shield bar's gradient. */
    private static final Color SHIELD_DARK = new Color(30, 60, 90);
    
    /** The color for the empty portion of the status bars. */
    private static final Color EMPTY_COLOR = new Color(20, 25, 35, 200);

    /** The font for labels (not currently used). */
    private static final Font LABEL_FONT = new Font("Georgia", Font.ITALIC, 11);

    /** The width of the status bars. */
    private static final int BAR_WIDTH = 200;
    /** The height of the status bars. */
    private static final int BAR_HEIGHT = 20;
    /** The size of the decorative mask icons next to the bars. */
    private static final int MASK_SIZE = 24; // Height of the mask icons
    /** The padding around the component's content. */
    private static final int PADDING = 12;
    /** The vertical spacing between the health and shield bars. */
    private static final int BAR_SPACING = 10;
    /** The corner radius for rounded rectangles. */
    private static final int CORNER_RADIUS = 12;

    /**
     * Constructs a new StatusBarsComponent, setting its default size and properties.
     */
    public StatusBarsComponent() {
        setOpaque(false);
        setFocusable(false);
        
        int totalWidth = MASK_SIZE + 8 + BAR_WIDTH + 2 * PADDING;
        int totalHeight = (BAR_HEIGHT * 2) + BAR_SPACING + (PADDING * 2);
        setPreferredSize(new Dimension(totalWidth, totalHeight));
    }

    /**
     * The main rendering method for the component. 
     * @param g The Graphics context to paint on.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        drawBackgroundPanel(g2d);
        
        int currentY = PADDING;
        int maskX = PADDING;
        int barX = maskX + MASK_SIZE + 8;
        
        drawHealthMask(g2d, maskX, currentY - 2);
        drawOrnateBar(g2d, barX, currentY, BAR_WIDTH, BAR_HEIGHT, 
                      health / 100.0, HEALTH_COLOR, HEALTH_GLOW, HEALTH_DARK);
        
        currentY += BAR_HEIGHT + BAR_SPACING;
        
        drawShieldMask(g2d, maskX, currentY - 2);
        drawOrnateBar(g2d, barX, currentY, BAR_WIDTH, BAR_HEIGHT, 
                      shield / 100.0, SHIELD_COLOR, SHIELD_GLOW, SHIELD_DARK);
        
        g2d.dispose();
    }

    /**
     * Draws the main background panel for the component, including a shadow,
     * gradient fill, and border.
     * @param g2d The Graphics2D context to draw on.
     */
    private void drawBackgroundPanel(Graphics2D g2d) {
        int panelWidth = getWidth() - 4;
        int panelHeight = getHeight() - 4;
        
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(3, 3, panelWidth, panelHeight, CORNER_RADIUS, CORNER_RADIUS);
        
        LinearGradientPaint bgGradient = new LinearGradientPaint(
            0, 0, 0, panelHeight,
            new float[]{0f, 1f},
            new Color[]{
                new Color(25, 30, 40, 160),
                new Color(15, 20, 30, 180)
            }
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(2, 2, panelWidth, panelHeight, CORNER_RADIUS, CORNER_RADIUS);
        
        g2d.setColor(FRAME_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(2, 2, panelWidth, panelHeight, CORNER_RADIUS, CORNER_RADIUS);
    }

    /**
     * Draws the stylized mask icon for the health bar.
     * @param g2d The Graphics2D context to draw on.
     * @param x The x-coordinate for the icon's position.
     * @param y The y-coordinate for the icon's position.
     */
    private void drawHealthMask(Graphics2D g2d, int x, int y) {
        Path2D.Double mask = new Path2D.Double();
        int cx = x + MASK_SIZE/2;
        int cy = y + MASK_SIZE/2;
        
        mask.moveTo(cx, cy - MASK_SIZE/2);
        mask.curveTo(cx + MASK_SIZE/3, cy - MASK_SIZE/3, 
                     cx + MASK_SIZE/2, cy, 
                     cx + MASK_SIZE/3, cy + MASK_SIZE/3);
        mask.curveTo(cx + MASK_SIZE/4, cy + MASK_SIZE/2, 
                     cx - MASK_SIZE/4, cy + MASK_SIZE/2, 
                     cx - MASK_SIZE/3, cy + MASK_SIZE/3);
        mask.curveTo(cx - MASK_SIZE/2, cy, 
                     cx - MASK_SIZE/3, cy - MASK_SIZE/3, 
                     cx, cy - MASK_SIZE/2);
        mask.closePath();
        
        g2d.setColor(new Color(HEALTH_DARK.getRed(), HEALTH_DARK.getGreen(), 
                              HEALTH_DARK.getBlue(), 100));
        g2d.fill(mask);
        
        g2d.setColor(new Color(HEALTH_GLOW.getRed(), HEALTH_GLOW.getGreen(), 
                              HEALTH_GLOW.getBlue(), 60));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(mask);
        
        g2d.setColor(new Color(HEALTH_COLOR.getRed(), HEALTH_COLOR.getGreen(), 
                              HEALTH_COLOR.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(mask);
    }

    /**
     * Draws the stylized cocoon icon for the shield bar.
     * @param g2d The Graphics2D context to draw on.
     * @param x The x-coordinate for the icon's position.
     * @param y The y-coordinate for the icon's position.
     */
    private void drawShieldMask(Graphics2D g2d, int x, int y) {
        Path2D.Double cocoon = new Path2D.Double();
        int cx = x + MASK_SIZE/2;
        int cy = y + MASK_SIZE/2;
        
        cocoon.moveTo(cx, cy - MASK_SIZE/2);
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            double r = MASK_SIZE/2 * (i % 2 == 0 ? 0.9 : 0.7);
            double px = cx + r * Math.sin(angle);
            double py = cy - r * Math.cos(angle);
            if (i == 0) {
                cocoon.moveTo(px, py);
            } else {
                cocoon.lineTo(px, py);
            }
        }
        cocoon.closePath();
        
        RadialGradientPaint shieldGradient = new RadialGradientPaint(
            new Point2D.Float(cx, cy), MASK_SIZE/2,
            new float[]{0f, 0.7f, 1f},
            new Color[]{
                new Color(SHIELD_GLOW.getRed(), SHIELD_GLOW.getGreen(), 
                         SHIELD_GLOW.getBlue(), 100),
                new Color(SHIELD_COLOR.getRed(), SHIELD_COLOR.getGreen(), 
                         SHIELD_COLOR.getBlue(), 150),
                new Color(SHIELD_DARK.getRed(), SHIELD_DARK.getGreen(), 
                         SHIELD_DARK.getBlue(), 100)
            }
        );
        g2d.setPaint(shieldGradient);
        g2d.fill(cocoon);
        
        g2d.setColor(new Color(SHIELD_GLOW.getRed(), SHIELD_GLOW.getGreen(), 
                              SHIELD_GLOW.getBlue(), 80));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(cocoon);
    }

    /**
     * Draws a single, complete, ornate status bar.
     * @param g2d The Graphics2D context to draw on.
     * @param x The x-coordinate of the bar.
     * @param y The y-coordinate of the bar.
     * @param width The width of the bar.
     * @param height The height of the bar.
     * @param fillPercent The percentage (0.0 to 1.0) of the bar that should be filled.
     * @param mainColor The primary color for the filled portion.
     * @param glowColor The highlight/glow color for effects.
     * @param darkColor The shadow/bottom color for the gradient.
     */
    private void drawOrnateBar(Graphics2D g2d, int x, int y, int width, int height, 
                              double fillPercent, Color mainColor, Color glowColor, Color darkColor) {
        g2d.setColor(EMPTY_COLOR);
        g2d.fillRoundRect(x, y, width, height, height/2, height/2);
        
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawRoundRect(x + 1, y + 1, width - 2, height - 2, height/2 - 1, height/2 - 1);
        
        if (fillPercent > 0) {
            int fillWidth = (int)(width * fillPercent);
            
            LinearGradientPaint fillGradient = new LinearGradientPaint(
                x, y, x, y + height,
                new float[]{0f, 0.3f, 0.7f, 1f},
                new Color[]{
                    glowColor,
                    mainColor,
                    mainColor,
                    darkColor
                }
            );
            
            g2d.setPaint(fillGradient);
            g2d.fillRoundRect(x, y, fillWidth, height, height/2, height/2);
            
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(glowColor);
            g2d.fillRoundRect(x + 2, y + 2, fillWidth - 4, height/3, height/4, height/4);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            if (fillPercent < 1.0) {
                long time = System.currentTimeMillis();
                float pulse = (float)(Math.sin(time * 0.005) * 0.3 + 0.7);
                g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), 
                                      glowColor.getBlue(), (int)(100 * pulse)));
                g2d.fillOval(x + fillWidth - height/2, y, height, height);
            }
        }
        
        g2d.setColor(FRAME_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, height/2, height/2);
        
        drawCornerDecoration(g2d, x - 2, y - 2);
        drawCornerDecoration(g2d, x + width - 6, y - 2);
    }

    /**
     * Draws a small decorative element for the corners of the status bars.
     * @param g2d The Graphics2D context to draw on.
     * @param x The x-coordinate of the decoration.
     * @param y The y-coordinate of the decoration.
     */
    private void drawCornerDecoration(Graphics2D g2d, int x, int y) {
        g2d.setColor(FRAME_COLOR);
        Path2D.Double decoration = new Path2D.Double();
        decoration.moveTo(x + 4, y);
        decoration.lineTo(x + 8, y + 4);
        decoration.lineTo(x + 4, y + 8);
        decoration.lineTo(x, y + 4);
        decoration.closePath();
        g2d.fill(decoration);
    }

    /**
     * Updates the health value to be displayed. 
     * @param health The new health percentage (0-100).
     */
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(100, health));
        repaint();
    }

    /**
     * Updates the shield value to be displayed. 
     * @param shield The new shield percentage (0-100).
     */
    public void setShield(int shield) {
        this.shield = Math.max(0, Math.min(100, shield));
        repaint();
    }
}