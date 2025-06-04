package com.tavuc.ui.components;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.RadialGradientPaint;
import java.awt.BasicStroke;
import java.awt.LinearGradientPaint;
import java.awt.AlphaComposite;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class DialogComponent extends JPanel {
    private JTextArea dialogTextArea;
    private JPanel characterPanel;
    private BufferedImage characterGlow;

    // Hollow Knight inspired colors
    private static final Color BORDER_COLOR = new Color(180, 190, 210, 100);
    private static final Color TEXT_COLOR = new Color(220, 230, 240);
    private static final Color ACCENT_COLOR = new Color(100, 180, 220);
    private static final Color BG_COLOR = new Color(10, 15, 25, 200);
    private static final Color GLOW_COLOR = new Color(120, 200, 255, 60);
    
    private static final Font DIALOG_FONT = new Font("Georgia", Font.ITALIC, 13);
    private static final Font ICON_FONT = new Font("Georgia", Font.BOLD, 32);

    private static final int CHARACTER_SIZE = 70;
    private static final int DIALOG_WIDTH = 320;
    private static final int DIALOG_HEIGHT = 90;
    private static final int PADDING = 15;
    private static final int HORIZONTAL_GAP = 12;

    public DialogComponent() {
        setFocusable(false);
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        int totalWidth = CHARACTER_SIZE + HORIZONTAL_GAP + DIALOG_WIDTH + 2 * PADDING;
        int totalHeight = Math.max(CHARACTER_SIZE, DIALOG_HEIGHT) + 2 * PADDING;
        setPreferredSize(new Dimension(totalWidth, totalHeight));

        // Character portrait panel
        characterPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                drawCharacterPortrait(g2d);
                g2d.dispose();
            }
        };
        characterPanel.setPreferredSize(new Dimension(CHARACTER_SIZE, CHARACTER_SIZE));
        characterPanel.setOpaque(false);
        characterPanel.setFocusable(false);

        // Dialog text area with custom styling
        dialogTextArea = new JTextArea("...");
        dialogTextArea.setEditable(false);
        dialogTextArea.setFocusable(false);
        dialogTextArea.setLineWrap(true);
        dialogTextArea.setWrapStyleWord(true);
        dialogTextArea.setFont(DIALOG_FONT);
        dialogTextArea.setForeground(TEXT_COLOR);
        dialogTextArea.setBackground(new Color(0, 0, 0, 0));
        dialogTextArea.setOpaque(false);
        dialogTextArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Custom panel for dialog background
        JPanel dialogPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                drawDialogBackground(g2d);
                g2d.dispose();
            }
        };
        dialogPanel.setOpaque(false);
        dialogPanel.add(dialogTextArea, BorderLayout.CENTER);
        dialogPanel.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(HORIZONTAL_GAP, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        contentPanel.add(characterPanel, BorderLayout.WEST);
        contentPanel.add(dialogPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
        
        // Initialize character glow effect
        createCharacterGlow();
    }

    private void drawCharacterPortrait(Graphics2D g2d) {
        int centerX = CHARACTER_SIZE / 2;
        int centerY = CHARACTER_SIZE / 2;
        
        // Animated glow effect
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time * 0.002) * 0.2 + 0.8);
        
        // Draw glow
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f * pulse));
        g2d.drawImage(characterGlow, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        // Outer mystical circle
        g2d.setColor(new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), 
                              ACCENT_COLOR.getBlue(), (int)(80 * pulse)));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(5, 5, CHARACTER_SIZE - 10, CHARACTER_SIZE - 10);
        
        // Inner frame with gradient
        LinearGradientPaint frameGradient = new LinearGradientPaint(
            0, 0, 0, CHARACTER_SIZE,
            new float[]{0f, 0.5f, 1f},
            new Color[]{
                new Color(60, 80, 100, 150),
                new Color(40, 60, 80, 180),
                new Color(20, 30, 50, 200)
            }
        );
        g2d.setPaint(frameGradient);
        g2d.fillOval(10, 10, CHARACTER_SIZE - 20, CHARACTER_SIZE - 20);
        
        // Character silhouette (vessel-like shape)
        drawVesselShape(g2d, centerX, centerY, 20);
        
        // Decorative corners
        drawPortraitDecorations(g2d);
    }

    private void drawVesselShape(Graphics2D g2d, int cx, int cy, int size) {
        Path2D.Double vessel = new Path2D.Double();
        
        // Create a hollow knight inspired vessel shape
        vessel.moveTo(cx, cy - size);
        vessel.curveTo(cx + size * 0.6, cy - size * 0.8,
                      cx + size * 0.8, cy - size * 0.2,
                      cx + size * 0.7, cy + size * 0.3);
        vessel.curveTo(cx + size * 0.5, cy + size * 0.8,
                      cx - size * 0.5, cy + size * 0.8,
                      cx - size * 0.7, cy + size * 0.3);
        vessel.curveTo(cx - size * 0.8, cy - size * 0.2,
                      cx - size * 0.6, cy - size * 0.8,
                      cx, cy - size);
        vessel.closePath();
        
        // Fill with gradient
        RadialGradientPaint vesselGradient = new RadialGradientPaint(
            cx, cy, size,
            new float[]{0f, 0.6f, 1f},
            new Color[]{
                new Color(200, 220, 240, 200),
                new Color(100, 140, 180, 180),
                new Color(40, 60, 80, 160)
            }
        );
        g2d.setPaint(vesselGradient);
        g2d.fill(vessel);
        
        // Add highlight
        g2d.setColor(new Color(255, 255, 255, 100));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(vessel);
    }

    private void drawPortraitDecorations(Graphics2D g2d) {
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        
        // Corner flourishes
        int[][] corners = {{0, 0}, {CHARACTER_SIZE, 0}, {0, CHARACTER_SIZE}, {CHARACTER_SIZE, CHARACTER_SIZE}};
        int[] xDir = {1, -1, 1, -1};
        int[] yDir = {1, 1, -1, -1};
        
        for (int i = 0; i < 4; i++) {
            Path2D.Double flourish = new Path2D.Double();
            int x = corners[i][0];
            int y = corners[i][1];
            flourish.moveTo(x, y + yDir[i] * 15);
            flourish.curveTo(x + xDir[i] * 5, y + yDir[i] * 10,
                           x + xDir[i] * 10, y + yDir[i] * 5,
                           x + xDir[i] * 15, y);
            g2d.draw(flourish);
        }
    }

    private void drawDialogBackground(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRoundRect(2, 2, w - 4, h - 4, 15, 15);
        
        // Main background with gradient
        LinearGradientPaint bgGradient = new LinearGradientPaint(
            0, 0, 0, h,
            new float[]{0f, 0.5f, 1f},
            new Color[]{
                new Color(BG_COLOR.getRed(), BG_COLOR.getGreen(), BG_COLOR.getBlue(), 220),
                new Color(15, 20, 30, 240),
                new Color(10, 15, 25, 250)
            }
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(0, 0, w, h, 15, 15);
        
        // Ornate border
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(0, 0, w - 1, h - 1, 15, 15);
        
        // Inner glow
        g2d.setColor(new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), 
                              GLOW_COLOR.getBlue(), 40));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, w - 5, h - 5, 13, 13);
        
        // Speech indicator (small triangle)
        drawSpeechIndicator(g2d, 15, h - 10);
    }

    private void drawSpeechIndicator(Graphics2D g2d, int x, int y) {
        long time = System.currentTimeMillis();
        float alpha = (float)(Math.sin(time * 0.003) * 0.3 + 0.7);
        
        g2d.setColor(new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), 
                              TEXT_COLOR.getBlue(), (int)(150 * alpha)));
        
        Path2D.Double indicator = new Path2D.Double();
        indicator.moveTo(x, y);
        indicator.lineTo(x + 8, y - 5);
        indicator.lineTo(x + 8, y + 5);
        indicator.closePath();
        g2d.fill(indicator);
    }

    private void createCharacterGlow() {
        characterGlow = new BufferedImage(CHARACTER_SIZE, CHARACTER_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = characterGlow.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Create radial glow
        RadialGradientPaint glowGradient = new RadialGradientPaint(
            CHARACTER_SIZE / 2f, CHARACTER_SIZE / 2f, CHARACTER_SIZE / 2f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{
                new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 100),
                new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 40),
                new Color(0, 0, 0, 0)
            }
        );
        g2d.setPaint(glowGradient);
        g2d.fillOval(0, 0, CHARACTER_SIZE, CHARACTER_SIZE);
        g2d.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Trigger repaint for animations
        repaint(50);
    }

    public void setDialogText(String text) {
        dialogTextArea.setText(text);
    }

    public void setIcon(/* Some Icon representation */) {
        // Update character portrait when implemented
        characterPanel.repaint();
    }
}