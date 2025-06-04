package com.tavuc.ui.components;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.RadialGradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.AlphaComposite;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.tavuc.managers.InputManager;

public class MovementKeysComponent extends JPanel {
    private OrnatekeyButton wButton, aButton, sButton, dButton;
    private InputManager inputManager;

    // Hollow Knight inspired colors
    private static final Color BORDER_COLOR = new Color(180, 190, 210, 100);
    private static final Color TEXT_COLOR = new Color(220, 230, 240);
    private static final Color KEY_IDLE_COLOR = new Color(30, 35, 50, 180);
    private static final Color KEY_HOVER_COLOR = new Color(50, 60, 80, 200);
    private static final Color KEY_ACTIVE_COLOR = new Color(100, 180, 220);
    private static final Color GLOW_COLOR = new Color(120, 200, 255, 80);
    
    private static final Font KEY_FONT = new Font("Georgia", Font.BOLD, 18);
    
    private static final int BUTTON_SIZE = 48;
    private static final int PADDING = 12;
    private static final int GRID_GAP = 6;

    public MovementKeysComponent(InputManager inputManager) {
        this.inputManager = inputManager;
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        
        int gridWidth = BUTTON_SIZE * 3 + GRID_GAP * 2;
        int gridHeight = BUTTON_SIZE * 2 + GRID_GAP;
        setPreferredSize(new Dimension(gridWidth + 2 * PADDING, gridHeight + 2 * PADDING));

        JPanel keysPanel = new JPanel(new GridLayout(2, 3, GRID_GAP, GRID_GAP)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                drawBackgroundDecoration(g2d);
                g2d.dispose();
            }
        };
        keysPanel.setOpaque(false);
        keysPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        wButton = createKeyButton("W", KeyEvent.VK_W);
        aButton = createKeyButton("A", KeyEvent.VK_A);
        sButton = createKeyButton("S", KeyEvent.VK_S);
        dButton = createKeyButton("D", KeyEvent.VK_D);

        JPanel emptyTopLeft = new JPanel();
        emptyTopLeft.setOpaque(false);
        JPanel emptyTopRight = new JPanel();
        emptyTopRight.setOpaque(false);

        keysPanel.add(emptyTopLeft);
        keysPanel.add(wButton);
        keysPanel.add(emptyTopRight);
        keysPanel.add(aButton);
        keysPanel.add(sButton);
        keysPanel.add(dButton);
        
        add(keysPanel, java.awt.BorderLayout.CENTER);
    }

    private void drawBackgroundDecoration(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        
        // Subtle background with gradient
        LinearGradientPaint bgGradient = new LinearGradientPaint(
            0, 0, w, h,
            new float[]{0f, 1f},
            new Color[]{
                new Color(15, 20, 30, 100),
                new Color(10, 15, 25, 120)
            }
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(0, 0, w, h, 20, 20);
        
        // Ornamental frame
        g2d.setColor(new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), 
                              BORDER_COLOR.getBlue(), 60));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(2, 2, w - 4, h - 4, 18, 18);
    }

    private OrnatekeyButton createKeyButton(String text, int keyCode) {
        OrnatekeyButton button = new OrnatekeyButton(text);
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setFocusable(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                inputManager.simulateKeyPress(keyCode);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                inputManager.simulateKeyRelease(keyCode);
            }
        });
        return button;
    }

    public void updateKeyStates(boolean wPressed, boolean aPressed, boolean sPressed, boolean dPressed) {
        wButton.setPressed(wPressed);
        aButton.setPressed(aPressed);
        sButton.setPressed(sPressed);
        dButton.setPressed(dPressed);
    }

    private class OrnatekeyButton extends JButton {
        private boolean isPressed = false;
        private boolean isHovered = false;
        private float glowIntensity = 0f;
        private javax.swing.Timer glowTimer;

        public OrnatekeyButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(KEY_FONT);
            
            glowTimer = new javax.swing.Timer(50, e -> {
                if (isPressed) {
                    glowIntensity = Math.min(1f, glowIntensity + 0.1f);
                } else {
                    glowIntensity = Math.max(0f, glowIntensity - 0.05f);
                }
                repaint();
                
                if (!isPressed && glowIntensity <= 0) {
                    glowTimer.stop();
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }
        
        public void setPressed(boolean pressed) {
            if (this.isPressed != pressed) {
                this.isPressed = pressed;
                if (pressed && !glowTimer.isRunning()) {
                    glowTimer.start();
                }
                repaint();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int size = Math.min(getWidth(), getHeight());
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            
            // Draw glow effect when pressed
            if (glowIntensity > 0) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.6f));
                RadialGradientPaint glowGradient = new RadialGradientPaint(
                    new Point2D.Float(getWidth() / 2f, getHeight() / 2f),
                    size * 0.8f,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{
                        new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 120),
                        new Color(GLOW_COLOR.getRed(), GLOW_COLOR.getGreen(), GLOW_COLOR.getBlue(), 60),
                        new Color(0, 0, 0, 0)
                    }
                );
                g2d.setPaint(glowGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
            
            // Draw key shape (organic rounded square)
            Path2D.Double keyShape = createOrganicKeyShape(x, y, size);
            
            // Key background
            Color bgColor = isPressed ? KEY_ACTIVE_COLOR : (isHovered ? KEY_HOVER_COLOR : KEY_IDLE_COLOR);
            if (isPressed) {
                // Active gradient
                RadialGradientPaint activeGradient = new RadialGradientPaint(
                    new Point2D.Float(x + size/2f, y + size/2f),
                    size/2f,
                    new float[]{0f, 0.7f, 1f},
                    new Color[]{
                        new Color(KEY_ACTIVE_COLOR.getRed(), KEY_ACTIVE_COLOR.getGreen(), 
                                 KEY_ACTIVE_COLOR.getBlue(), 220),
                        new Color(KEY_ACTIVE_COLOR.getRed(), KEY_ACTIVE_COLOR.getGreen(), 
                                 KEY_ACTIVE_COLOR.getBlue(), 180),
                        new Color(KEY_ACTIVE_COLOR.getRed()/2, KEY_ACTIVE_COLOR.getGreen()/2, 
                                 KEY_ACTIVE_COLOR.getBlue()/2, 160)
                    }
                );
                g2d.setPaint(activeGradient);
            } else {
                g2d.setColor(bgColor);
            }
            g2d.fill(keyShape);
            
            // Border with inner shadow effect
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.setStroke(new BasicStroke(1));
            g2d.draw(keyShape);
            
            g2d.setColor(BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(keyShape);
            
            // Inner highlight
            if (isPressed) {
                g2d.setColor(new Color(255, 255, 255, 60));
                Path2D.Double innerHighlight = createOrganicKeyShape(x + 3, y + 3, size - 6);
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(innerHighlight);
            }
            
            // Draw text
            g2d.setFont(getFont());
            g2d.setColor(isPressed ? new Color(20, 25, 35) : TEXT_COLOR);
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            // Text shadow
            if (!isPressed) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(getText(), textX + 1, textY + 1);
                g2d.setColor(TEXT_COLOR);
            }
            g2d.drawString(getText(), textX, textY);
            
            g2d.dispose();
        }
        
        private Path2D.Double createOrganicKeyShape(int x, int y, int size) {
            Path2D.Double shape = new Path2D.Double();
            int cornerRadius = size / 4;
            
            // Create slightly irregular rounded rectangle
            shape.moveTo(x + cornerRadius, y);
            shape.lineTo(x + size - cornerRadius, y);
            shape.quadTo(x + size, y, x + size, y + cornerRadius);
            shape.lineTo(x + size, y + size - cornerRadius);
            shape.quadTo(x + size, y + size, x + size - cornerRadius, y + size);
            shape.lineTo(x + cornerRadius, y + size);
            shape.quadTo(x, y + size, x, y + size - cornerRadius);
            shape.lineTo(x, y + cornerRadius);
            shape.quadTo(x, y, x + cornerRadius, y);
            shape.closePath();
            
            return shape;
        }
    }
}