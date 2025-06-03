package com.tavuc.ui.panels;

import javax.swing.*;

import com.tavuc.utils.FrameControllable;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter; 

public class TitleBar extends JPanel {

    private JLabel titleLabel;
    private JLabel iconLabel;
    private CustomButton minimizeButton;
    private CustomButton maximizeButton;
    private CustomButton closeButton;
    private FrameControllable parentFrame; 

    private int pX, pY;
    
    private static final Color BACKGROUND_COLOR = new Color(32, 32, 32);
    private static final Color BUTTON_HOVER_COLOR = new Color(60, 60, 60);
    private static final Color CLOSE_HOVER_COLOR = new Color(232, 17, 35);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final Color SECONDARY_TEXT_COLOR = new Color(200, 200, 200);

    public TitleBar(FrameControllable frame) { 
        this.parentFrame = frame;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(parentFrame.getFrame().getWidth(), 32));
        
        iconLabel = new JLabel("◆");
        iconLabel.setForeground(new Color(0, 120, 215));
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));

        titleLabel = new JLabel("Untitled"); 
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        Dimension buttonSize = new Dimension(45, 32); 
        
        minimizeButton = new CustomButton("🗕", ButtonType.MINIMIZE);
        minimizeButton.setPreferredSize(buttonSize);
        minimizeButton.setToolTipText("Minimize");
        minimizeButton.addActionListener(e -> parentFrame.performMinimize());

        maximizeButton = new CustomButton("🗖", ButtonType.MAXIMIZE);
        maximizeButton.setPreferredSize(buttonSize);
        maximizeButton.setToolTipText("Maximize");
        maximizeButton.addActionListener(e -> parentFrame.performMaximizeRestore());

        closeButton = new CustomButton("✕", ButtonType.CLOSE);
        closeButton.setPreferredSize(buttonSize);
        closeButton.setToolTipText("Close");
        closeButton.addActionListener(e -> parentFrame.performClose());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0)); 
        leftPanel.add(iconLabel);
        leftPanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(minimizeButton);
        buttonPanel.add(maximizeButton);
        buttonPanel.add(closeButton);

        add(leftPanel, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.EAST);
    }

    private void setupEventHandlers() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (parentFrame.getFrame().getExtendedState() != JFrame.MAXIMIZED_BOTH) { 
                    pX = e.getX();
                    pY = e.getY();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    parentFrame.performMaximizeRestore();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (parentFrame.getFrame().getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    parentFrame.getFrame().setLocation(
                        parentFrame.getFrame().getLocation().x + e.getX() - pX,
                        parentFrame.getFrame().getLocation().y + e.getY() - pY
                    );
                }
            }
        });
    }

    public void updateTitle(String title) {
        titleLabel.setText(title);
    }

    public void updateMaximizeButtonText(boolean isMaximized) {
        if (isMaximized) {
            maximizeButton.setText("🗗"); 
        } else {
            maximizeButton.setText("🗖"); 
        }
    }

    private static class CustomButton extends JButton {
        private ButtonType type;
        private boolean isPressed = false;
        private Timer hoverTimer;
        private float hoverAnimationProgress = 0.0f; 
        private boolean animatingHoverIn = false;

        public CustomButton(String text, ButtonType type) {
            super(text);
            this.type = type;
            setupButton();
        }

        private void setupButton() {
            setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
            setForeground(TEXT_COLOR);
            setBackground(BACKGROUND_COLOR); 
            setBorder(BorderFactory.createEmptyBorder());
            setFocusPainted(false);
            setContentAreaFilled(false); 

            hoverTimer = new Timer(15, e -> animateHover()); 
            hoverTimer.setRepeats(true);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    animatingHoverIn = true;
                    if (!hoverTimer.isRunning() || hoverAnimationProgress < 1.0f) {
                        hoverTimer.start();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    animatingHoverIn = false;
                    if (!hoverTimer.isRunning() || hoverAnimationProgress > 0.0f) {
                        hoverTimer.start();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (isPressed) {
                            isPressed = false;
                            Point p = e.getPoint();
                            boolean stillOver = p.x >= 0 && p.x < getWidth() && p.y >= 0 && p.y < getHeight();
                            if (!stillOver) {
                                animatingHoverIn = false; 
                                if (!hoverTimer.isRunning() || hoverAnimationProgress > 0.0f) {
                                     hoverTimer.start();
                                }
                            }
                            repaint();
                        }
                    }
                }
            });
        }

        private void animateHover() {
            float step = 0.08f; 
            if (animatingHoverIn) {
                hoverAnimationProgress += step;
                if (hoverAnimationProgress >= 1.0f) {
                    hoverAnimationProgress = 1.0f;
                    hoverTimer.stop();
                }
            } else { 
                hoverAnimationProgress -= step;
                if (hoverAnimationProgress <= 0.0f) {
                    hoverAnimationProgress = 0.0f;
                    hoverTimer.stop();
                }
            }
            repaint();
        }

        private static Color interpolateColor(Color color1, Color color2, float fraction) {
            fraction = Math.max(0f, Math.min(1f, fraction));
            int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * fraction);
            int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * fraction);
            int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * fraction);
            int a = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * fraction);
            return new Color(r, g, b, a);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color baseBgColor = getBackground(); 
            Color hoverTargetColor = (type == ButtonType.CLOSE) ? CLOSE_HOVER_COLOR : BUTTON_HOVER_COLOR;
            
            Color currentBgColor = interpolateColor(baseBgColor, hoverTargetColor, hoverAnimationProgress);

            if (isPressed) {
                if (type == ButtonType.CLOSE) {
                    currentBgColor = new Color(190, 10, 25); 
                } else {
                    currentBgColor = BUTTON_HOVER_COLOR.darker();
                }
            }
            
            g2d.setColor(currentBgColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            Color currentTextColor = getForeground(); 
            if (type == ButtonType.CLOSE) {
                if (hoverAnimationProgress > 0.5f || isPressed) {
                    currentTextColor = Color.WHITE;
                }
            }
         

            g2d.setColor(currentTextColor);
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            if (type == ButtonType.MINIMIZE) {
                y -= 2; 
            }
            g2d.drawString(getText(), x, y);

            g2d.dispose();
        }
    }

    private enum ButtonType {
        MINIMIZE, MAXIMIZE, CLOSE
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, BACKGROUND_COLOR,
            0, getHeight(), new Color(28, 28, 28)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.setColor(new Color(50, 50, 50));
        g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        
        g2d.dispose();
    }
}
