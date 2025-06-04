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

public class StatusBarsComponent extends JPanel {
    private int health = 100; 
    private int shield = 75;  

    // Hollow Knight inspired colors
    private static final Color FRAME_COLOR = new Color(180, 190, 210, 120);
    private static final Color FRAME_SHADOW = new Color(10, 15, 25, 180);
    private static final Color TEXT_COLOR = new Color(200, 210, 230, 200);
    
    // Health uses deep red/crimson like soul vessels
    private static final Color HEALTH_COLOR = new Color(220, 60, 40);
    private static final Color HEALTH_GLOW = new Color(255, 100, 80);
    private static final Color HEALTH_DARK = new Color(80, 20, 15);
    
    // Shield uses ethereal blue like lifeblood
    private static final Color SHIELD_COLOR = new Color(80, 160, 220);
    private static final Color SHIELD_GLOW = new Color(120, 200, 255);
    private static final Color SHIELD_DARK = new Color(30, 60, 90);
    
    private static final Color EMPTY_COLOR = new Color(20, 25, 35, 200);

    private static final Font LABEL_FONT = new Font("Georgia", Font.ITALIC, 11);

    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int MASK_SIZE = 24; // Height of the mask icons
    private static final int PADDING = 12;
    private static final int BAR_SPACING = 10;
    private static final int CORNER_RADIUS = 12;

    public StatusBarsComponent() {
        setOpaque(false);
        setFocusable(false);
        
        int totalWidth = MASK_SIZE + 8 + BAR_WIDTH + 2 * PADDING;
        int totalHeight = (BAR_HEIGHT * 2) + BAR_SPACING + (PADDING * 2);
        setPreferredSize(new Dimension(totalWidth, totalHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw background panel with ornate border
        drawBackgroundPanel(g2d);
        
        int currentY = PADDING;
        int maskX = PADDING;
        int barX = maskX + MASK_SIZE + 8;
        
        // Draw Health
        drawHealthMask(g2d, maskX, currentY - 2);
        drawOrnateBar(g2d, barX, currentY, BAR_WIDTH, BAR_HEIGHT, 
                      health / 100.0, HEALTH_COLOR, HEALTH_GLOW, HEALTH_DARK);
        
        currentY += BAR_HEIGHT + BAR_SPACING;
        
        // Draw Shield
        drawShieldMask(g2d, maskX, currentY - 2);
        drawOrnateBar(g2d, barX, currentY, BAR_WIDTH, BAR_HEIGHT, 
                      shield / 100.0, SHIELD_COLOR, SHIELD_GLOW, SHIELD_DARK);
        
        g2d.dispose();
    }

    private void drawBackgroundPanel(Graphics2D g2d) {
        int panelWidth = getWidth() - 4;
        int panelHeight = getHeight() - 4;
        
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(3, 3, panelWidth, panelHeight, CORNER_RADIUS, CORNER_RADIUS);
        
        // Main panel with subtle gradient
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
        
        // Ornate border
        g2d.setColor(FRAME_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(2, 2, panelWidth, panelHeight, CORNER_RADIUS, CORNER_RADIUS);
    }

    private void drawHealthMask(Graphics2D g2d, int x, int y) {
        // Draw a vessel/mask shape for health
        Path2D.Double mask = new Path2D.Double();
        int cx = x + MASK_SIZE/2;
        int cy = y + MASK_SIZE/2;
        
        // Hollow Knight inspired mask shape
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
        
        // Fill with health color gradient
        g2d.setColor(new Color(HEALTH_DARK.getRed(), HEALTH_DARK.getGreen(), 
                              HEALTH_DARK.getBlue(), 100));
        g2d.fill(mask);
        
        // Glow effect
        g2d.setColor(new Color(HEALTH_GLOW.getRed(), HEALTH_GLOW.getGreen(), 
                              HEALTH_GLOW.getBlue(), 60));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(mask);
        
        // Inner highlight
        g2d.setColor(new Color(HEALTH_COLOR.getRed(), HEALTH_COLOR.getGreen(), 
                              HEALTH_COLOR.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(mask);
    }

    private void drawShieldMask(Graphics2D g2d, int x, int y) {
        // Draw a lifeblood cocoon shape for shield
        Path2D.Double cocoon = new Path2D.Double();
        int cx = x + MASK_SIZE/2;
        int cy = y + MASK_SIZE/2;
        
        // Organic cocoon shape
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
        
        // Fill with shield gradient
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
        
        // Crystalline effect
        g2d.setColor(new Color(SHIELD_GLOW.getRed(), SHIELD_GLOW.getGreen(), 
                              SHIELD_GLOW.getBlue(), 80));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(cocoon);
    }

    private void drawOrnateBar(Graphics2D g2d, int x, int y, int width, int height, 
                              double fillPercent, Color mainColor, Color glowColor, Color darkColor) {
        // Bar background (empty vessel)
        g2d.setColor(EMPTY_COLOR);
        g2d.fillRoundRect(x, y, width, height, height/2, height/2);
        
        // Inner shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.drawRoundRect(x + 1, y + 1, width - 2, height - 2, height/2 - 1, height/2 - 1);
        
        if (fillPercent > 0) {
            int fillWidth = (int)(width * fillPercent);
            
            // Create gradient for filled portion
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
            
            // Fill the bar
            g2d.setPaint(fillGradient);
            g2d.fillRoundRect(x, y, fillWidth, height, height/2, height/2);
            
            // Add shimmer effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(glowColor);
            g2d.fillRoundRect(x + 2, y + 2, fillWidth - 4, height/3, height/4, height/4);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            // Pulse effect at the end
            if (fillPercent < 1.0) {
                long time = System.currentTimeMillis();
                float pulse = (float)(Math.sin(time * 0.005) * 0.3 + 0.7);
                g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), 
                                      glowColor.getBlue(), (int)(100 * pulse)));
                g2d.fillOval(x + fillWidth - height/2, y, height, height);
            }
        }
        
        // Ornate frame
        g2d.setColor(FRAME_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, height/2, height/2);
        
        // Corner decorations
        drawCornerDecoration(g2d, x - 2, y - 2);
        drawCornerDecoration(g2d, x + width - 6, y - 2);
    }

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

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(100, health));
        repaint();
    }

    public void setShield(int shield) {
        this.shield = Math.max(0, Math.min(100, shield));
        repaint();
    }
}