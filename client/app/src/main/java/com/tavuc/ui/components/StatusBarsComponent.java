package com.tavuc.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Polygon;
import java.awt.geom.RoundRectangle2D;

public class StatusBarsComponent extends JPanel {
    private int health = 100; 
    private int shield = 75;  

    // Styled colors
    private static final Color BORDER_COLOR = new Color(70, 75, 85);
    private static final Color TEXT_COLOR = new Color(200, 200, 210);

    private static final Color HEALTH_BAR_FILL_COLOR = new Color(110, 20, 20); // Deep crimson
    private static final Color SHIELD_BAR_FILL_COLOR = new Color(30, 60, 120); // Deep blue
    private static final Color BAR_EMPTY_BG_COLOR = new Color(20, 25, 40); // Very dark base

    private static final Font UI_FONT_BOLD = new Font("Serif", Font.BOLD, 16); // Title
    private static final Font LABEL_FONT = new Font("Serif", Font.PLAIN, 12); // Bar labels

    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 18; // Slightly thinner than HTML concept for compactness
    private static final int SHIELD_TRIANGLE_WIDTH = 10;
    private static final int PADDING = 10;
    private static final int TITLE_AREA_HEIGHT = 30;
    private static final int LABEL_HEIGHT = 15;
    private static final int BAR_SPACING = 8; // Space between health and shield bar sections
    private static final int BORDER_THICKNESS = 2;
    private static final int ROUND_CORNER_ARC = 10; // For main component
    private static final int BAR_ROUND_CORNER_ARC = 6; // For individual bars

    public StatusBarsComponent() {
        setOpaque(false);
        setFocusable(false);
        
        int totalWidth = BAR_WIDTH + SHIELD_TRIANGLE_WIDTH + 2 * PADDING; // Max width consideration
        // Adjusted height: (BAR_HEIGHT * 2) for two bars, BAR_SPACING between them, and PADDING on top/bottom
        int totalHeight = (BAR_HEIGHT * 2) + BAR_SPACING + (PADDING * 2); 
        setPreferredSize(new Dimension(totalWidth, totalHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        java.awt.FontMetrics fmTitle = g2d.getFontMetrics(LABEL_FONT); // Use label font metrics for ascent if needed, though labels are removed

        // Health Bar
        int currentY = PADDING; // Start drawing from top padding

        // Health Bar Background (empty part)
        g2d.setColor(BAR_EMPTY_BG_COLOR);
        g2d.fill(new RoundRectangle2D.Float(PADDING, currentY, BAR_WIDTH, BAR_HEIGHT, BAR_ROUND_CORNER_ARC, BAR_ROUND_CORNER_ARC));
        // Health Bar Fill
        g2d.setColor(HEALTH_BAR_FILL_COLOR);
        int currentHealthWidth = (int) (BAR_WIDTH * (health / 100.0));
        g2d.fill(new RoundRectangle2D.Float(PADDING, currentY, currentHealthWidth, BAR_HEIGHT, BAR_ROUND_CORNER_ARC, BAR_ROUND_CORNER_ARC));
        // Health Bar Border
        g2d.setColor(BORDER_COLOR);
        g2d.draw(new RoundRectangle2D.Float(PADDING, currentY, BAR_WIDTH, BAR_HEIGHT, BAR_ROUND_CORNER_ARC, BAR_ROUND_CORNER_ARC));
        currentY += BAR_HEIGHT + BAR_SPACING;

        // Shield Bar
        // Shield Bar Background (empty part) - slightly shorter for triangle
        g2d.setColor(BAR_EMPTY_BG_COLOR);
        g2d.fill(new RoundRectangle2D.Float(PADDING, currentY, BAR_WIDTH, BAR_HEIGHT, BAR_ROUND_CORNER_ARC, BAR_ROUND_CORNER_ARC));
        // Shield Bar Fill (rectangular part)
        g2d.setColor(SHIELD_BAR_FILL_COLOR);
        int shieldRectWidth = (int) ((BAR_WIDTH - SHIELD_TRIANGLE_WIDTH) * (shield / 100.0));
        if (shield > 0) { // Only draw fill if shield is present
             g2d.fill(new RoundRectangle2D.Float(PADDING, currentY, shieldRectWidth, BAR_HEIGHT, BAR_ROUND_CORNER_ARC, BAR_ROUND_CORNER_ARC));
        }

        // Shield Bar Triangle End (only if there's some shield)
        if (shield > 0) {
            Polygon shieldTriangle = new Polygon();
            int triangleStartX = PADDING + shieldRectWidth;
            shieldTriangle.addPoint(triangleStartX, currentY); 
            shieldTriangle.addPoint(triangleStartX + SHIELD_TRIANGLE_WIDTH, currentY + BAR_HEIGHT / 2); 
            shieldTriangle.addPoint(triangleStartX, currentY + BAR_HEIGHT); 
            g2d.fillPolygon(shieldTriangle);
        }

        // Shield Bar Border (covers rect and triangle area)
        g2d.setColor(BORDER_COLOR);
        g2d.draw(new RoundRectangle2D.Float(PADDING, currentY, BAR_WIDTH - SHIELD_TRIANGLE_WIDTH, BAR_HEIGHT, BAR_ROUND_CORNER_ARC, BAR_ROUND_CORNER_ARC));
        // Draw border for the triangle part manually if it's not covered by a single RoundRect
        int fullBarEndX = PADDING + BAR_WIDTH - SHIELD_TRIANGLE_WIDTH;
        g2d.drawLine(fullBarEndX, currentY, fullBarEndX + SHIELD_TRIANGLE_WIDTH, currentY + BAR_HEIGHT / 2);
        g2d.drawLine(fullBarEndX + SHIELD_TRIANGLE_WIDTH, currentY + BAR_HEIGHT / 2, fullBarEndX, currentY + BAR_HEIGHT);
        if (shieldRectWidth < (BAR_WIDTH - SHIELD_TRIANGLE_WIDTH)) { // if shield not full, close the right edge of the drawn fill
             g2d.drawLine(PADDING + shieldRectWidth, currentY, PADDING + shieldRectWidth, currentY + BAR_HEIGHT);
        }


        g2d.dispose();
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
