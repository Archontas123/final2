package com.tavuc.ui.screens;


import java.awt.RadialGradientPaint;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.tavuc.Client;
import com.tavuc.managers.InputManager;

/**
 * Game Over screen displayed when the player's ship is destroyed.
 * Provides options to respawn or return to the main menu.
 */
public class GameOverScreen extends GScreen {
    
    // Player data
    private int playerId;
    private String username;
    
    // UI Components
    private JButton respawnButton;
    private JButton returnButton;
    
    // Visual effects
    private Timer animationTimer;
    private List<DebrisParticle> debrisParticles;
    private float explosionPhase = 0f;
    private static final int NUM_DEBRIS = 120;
    
    // Respawn countdown
    private int respawnCountdown = 5;
    private Timer respawnTimer;
    private boolean autoRespawn = false;
    
    // Ship status
    private String deathCause = "Ship Destroyed";
    private int finalScore = 0;
    
    /**
     * Creates a new GameOverScreen
     * 
     * @param playerId The player's ID
     * @param username The player's username
     */
    public GameOverScreen(int playerId, String username) {
        super();
        this.playerId = playerId;
        this.username = username;
        
        // Set screen title
        setTitle("Game Over - Space Combat");
        if (titleBarPanel != null) {
            titleBarPanel.updateTitle("Game Over - Space Combat");
        }
        
        // Initialize UI
        initializeUI();
        
        // Start animations
        initializeEffects();
        
        // Start respawn countdown if auto-respawn is enabled
        if (autoRespawn) {
            startRespawnCountdown();
        }
        
        // Add key listener for quick actions
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    returnToMainMenu();
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    respawnShip();
                }
            }
        });
    }
    
    /**
     * Initializes the UI components
     */
    private void initializeUI() {
        // Create content panel with custom painting
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background gradient
                drawBackground(g2d);
                
                // Draw particles
                drawParticles(g2d);
                
                // Draw explosion if in early phase
                if (explosionPhase < 1.0f) {
                    drawExplosion(g2d);
                }
                
                // Draw title and stats
                drawGameOverText(g2d);
            }
        };
        contentPanel.setBackground(new Color(10, 10, 15));
        
        // Set layout
        contentPanel.setLayout(new BorderLayout());
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));
        
        // Create respawn button
        respawnButton = new JButton("Respawn Ship");
        styleButton(respawnButton);
        respawnButton.addActionListener(e -> respawnShip());
        
        // Create return button
        returnButton = new JButton("Return to Navigation");
        styleButton(returnButton);
        returnButton.addActionListener(e -> returnToMainMenu());
        
        // Add buttons to panel
        buttonPanel.add(respawnButton);
        buttonPanel.add(returnButton);
        
        // Add button panel to bottom of content panel
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set content panel as the screen's content
        setScreenSpecificPanel(contentPanel);
        
        // Set preferred size and center on screen
        setPreferredSize(new Dimension(900, 650));
        if (!super.isActuallyFullScreen()) {
            pack();
            setLocationRelativeTo(null);
        }
    }
    
    /**
     * Initializes the visual effects
     */
    private void initializeEffects() {
        // Create debris particles
        debrisParticles = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < NUM_DEBRIS; i++) {
            DebrisParticle particle = new DebrisParticle(
                getWidth() / 2.0f + random.nextFloat() * 40 - 20,
                getHeight() / 2.0f + random.nextFloat() * 40 - 20,
                random.nextFloat() * 6 - 3,
                random.nextFloat() * 6 - 3,
                random.nextFloat() * 8 + 2,
                new Color(
                    random.nextFloat() * 0.5f + 0.5f,
                    random.nextFloat() * 0.3f,
                    random.nextFloat() * 0.1f,
                    random.nextFloat() * 0.5f + 0.5f
                )
            );
            debrisParticles.add(particle);
        }
        
        // Start animation timer
        animationTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update explosion phase
                explosionPhase += 0.01f;
                
                // Update particles
                for (DebrisParticle particle : debrisParticles) {
                    particle.update();
                }
                
                // Repaint
                repaint();
            }
        });
        animationTimer.start();
    }
    
    /**
     * Starts the respawn countdown timer
     */
    private void startRespawnCountdown() {
        respawnTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                respawnCountdown--;
                if (respawnCountdown <= 0) {
                    respawnTimer.stop();
                    respawnShip();
                } else {
                    respawnButton.setText("Respawn Ship (" + respawnCountdown + ")");
                }
            }
        });
        respawnTimer.start();
    }
    
    /**
     * Draws the background gradient
     */
    private void drawBackground(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        
        // Create radial gradient
        RadialGradientPaint gradient = new RadialGradientPaint(
            width / 2.0f, height / 2.0f, Math.max(width, height) / 1.5f,
            new float[] {0.0f, 0.5f, 1.0f},
            new Color[] {
                new Color(30, 15, 15),
                new Color(15, 5, 10),
                new Color(5, 0, 5)
            }
        );
        
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Draw stars
        g2d.setColor(new Color(255, 255, 255, 100));
        Random random = new Random(123); // Fixed seed for consistent stars
        for (int i = 0; i < 200; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(3) + 1;
            g2d.fillOval(x, y, size, size);
        }
    }
    
    /**
     * Draws all debris particles
     */
    private void drawParticles(Graphics2D g2d) {
        for (DebrisParticle particle : debrisParticles) {
            particle.draw(g2d);
        }
    }
    
    /**
     * Draws the explosion effect
     */
    private void drawExplosion(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // Calculate explosion size based on phase
        float size = 100 + explosionPhase * 300;
        float alpha = Math.max(0, 1.0f - explosionPhase);
        
        // Draw explosion glow
        Color[] colors = {
            new Color(1.0f, 0.8f, 0.3f, alpha * 0.8f),
            new Color(1.0f, 0.4f, 0.0f, alpha * 0.6f),
            new Color(0.8f, 0.0f, 0.0f, alpha * 0.4f),
            new Color(0.4f, 0.0f, 0.0f, alpha * 0.1f)
        };
        
        float[] sizes = {size * 0.4f, size * 0.7f, size * 0.9f, size};
        
        for (int i = 0; i < colors.length; i++) {
            g2d.setColor(colors[i]);
            float diameter = sizes[i];
            g2d.fillOval(
                Math.round(centerX - diameter/2),
                Math.round(centerY - diameter/2),
                Math.round(diameter),
                Math.round(diameter)
            );
        }
        
        // Draw shock wave if in early phase
        if (explosionPhase < 0.5f) {
            float waveSize = size * 1.2f;
            float waveAlpha = Math.max(0, 0.5f - explosionPhase) * 0.5f;
            g2d.setColor(new Color(1.0f, 1.0f, 1.0f, waveAlpha));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(
                Math.round(centerX - waveSize/2),
                Math.round(centerY - waveSize/2),
                Math.round(waveSize),
                Math.round(waveSize)
            );
        }
    }
    
    /**
     * Draws the game over text and stats
     */
    private void drawGameOverText(Graphics2D g2d) {
        int width = getWidth();
        int centerX = width / 2;
        int y = 100;
        
        // Draw main title
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        g2d.setFont(titleFont);
        String gameOverText = "SHIP DESTROYED";
        
        // Calculate title width for centering
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverText);
        
        // Draw title shadow
        g2d.setColor(new Color(100, 0, 0, 100));
        g2d.drawString(gameOverText, centerX - textWidth/2 + 3, y + 3);
        
        // Draw title
        g2d.setColor(new Color(255, 60, 60));
        g2d.drawString(gameOverText, centerX - textWidth/2, y);
        
        // Draw cause of death
        y += 60;
        Font causeFont = new Font("Arial", Font.ITALIC, 24);
        g2d.setFont(causeFont);
        g2d.setColor(new Color(200, 200, 200));
        
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(deathCause);
        g2d.drawString(deathCause, centerX - textWidth/2, y);
        
        // Draw player info
        y += 80;
        Font infoFont = new Font("Arial", Font.PLAIN, 18);
        g2d.setFont(infoFont);
        
        // Draw player name
        String playerText = "Pilot: " + username;
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(playerText);
        g2d.drawString(playerText, centerX - textWidth/2, y);
        
        // Draw score if available
        if (finalScore > 0) {
            y += 30;
            String scoreText = "Final Score: " + finalScore;
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(scoreText);
            g2d.drawString(scoreText, centerX - textWidth/2, y);
        }
        
        // Draw respawn message if auto-respawn is enabled
        if (autoRespawn && respawnCountdown > 0) {
            y += 60;
            String respawnText = "Auto-respawn in " + respawnCountdown + " seconds...";
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(new Color(100, 200, 100));
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(respawnText);
            g2d.drawString(respawnText, centerX - textWidth/2, y);
        }
        
        // Draw key hints
        y = getHeight() - 70;
        String hintText = "Press SPACE to respawn or ESC to return";
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(180, 180, 180, 180));
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(hintText);
        g2d.drawString(hintText, centerX - textWidth/2, y);
    }
    
    /**
     * Styles a button with a space game aesthetic
     */
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 100));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 140));
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 100));
            }
        });
    }
    
    /**
     * Respawns the player's ship and returns to the space screen
     */
    private void respawnShip() {
        // Stop timers
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (respawnTimer != null) {
            respawnTimer.stop();
        }
        
        // Open space screen with respawned ship
        dispose();
        SwingUtilities.invokeLater(() -> {
            new SpaceScreen(null, playerId, username).setVisible(true);
        });
    }
    
    /**
     * Returns to the main menu/ship interior
     */
    private void returnToMainMenu() {
        // Stop timers
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (respawnTimer != null) {
            respawnTimer.stop();
        }
        
        // Return to ship/main menu
        dispose();
        Client.returnToShip();
    }
    
    /**
     * Sets the cause of death to display
     */
    public void setDeathCause(String cause) {
        this.deathCause = cause;
        repaint();
    }
    
    /**
     * Sets the final score to display
     */
    public void setFinalScore(int score) {
        this.finalScore = score;
        repaint();
    }
    
    /**
     * Enables or disables auto-respawn
     */
    public void setAutoRespawn(boolean autoRespawn) {
        this.autoRespawn = autoRespawn;
        if (autoRespawn && respawnTimer == null) {
            startRespawnCountdown();
        } else if (!autoRespawn && respawnTimer != null) {
            respawnTimer.stop();
            respawnButton.setText("Respawn Ship");
        }
    }
    
    @Override
    public String getScreenTitle() {
        return "Game Over - Space Combat";
    }
    
    /**
     * Inner class representing a debris particle
     */
    private class DebrisParticle {
        private float x, y;
        private float vx, vy;
        private float size;
        private Color color;
        private float rotation = 0;
        private float rotationSpeed;
        private Path2D.Float shape;
        
        public DebrisParticle(float x, float y, float vx, float vy, float size, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.rotationSpeed = (float)(Math.random() * 0.2 - 0.1);
            
            // Create random polygon shape
            shape = new Path2D.Float();
            int points = 3 + (int)(Math.random() * 4);
            float[] angles = new float[points];
            float[] distances = new float[points];
            
            for (int i = 0; i < points; i++) {
                angles[i] = (float)(Math.random() * Math.PI * 2);
                distances[i] = (float)(0.5 + Math.random() * 0.5);
            }
            
            // Sort angles
            java.util.Arrays.sort(angles);
            
            // Create shape
            for (int i = 0; i < points; i++) {
                float angle = angles[i];
                float distance = distances[i] * size;
                float px = (float)(Math.cos(angle) * distance);
                float py = (float)(Math.sin(angle) * distance);
                
                if (i == 0) {
                    shape.moveTo(px, py);
                } else {
                    shape.lineTo(px, py);
                }
            }
            shape.closePath();
        }
        
        public void update() {
            x += vx;
            y += vy;
            rotation += rotationSpeed;
            
            // Apply slight drag
            vx *= 0.99f;
            vy *= 0.99f;
            
            // Apply slight gravity towards center
            float dx = getWidth() / 2.0f - x;
            float dy = getHeight() / 2.0f - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 50) {
                float force = 0.01f;
                vx += dx / distance * force;
                vy += dy / distance * force;
            }
        }
        
        public void draw(Graphics2D g2d) {
            AffineTransform oldTransform = g2d.getTransform();
            
            g2d.translate(x, y);
            g2d.rotate(rotation);
            
            g2d.setColor(color);
            g2d.fill(shape);
            
            g2d.setTransform(oldTransform);
        }
    }
}