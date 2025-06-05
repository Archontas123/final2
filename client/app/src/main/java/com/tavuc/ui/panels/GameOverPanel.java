package com.tavuc.ui.panels;

import com.tavuc.Client;
import com.tavuc.ui.screens.SpaceScreen;

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

/**
 * Panel shown when the player's ship is destroyed.
 * Displays an animated game over message with options to respawn
 * or return to navigation.
 */
public class GameOverPanel extends JPanel {

    private final SpaceScreen parentScreen;
    private final int playerId;
    private final String username;

    private JButton respawnButton;
    private JButton returnButton;

    private Timer animationTimer;
    private List<DebrisParticle> debrisParticles;
    private float explosionPhase = 0f;
    private static final int NUM_DEBRIS = 120;

    private int respawnCountdown = 5;
    private Timer respawnTimer;
    private boolean autoRespawn = false;

    private String deathCause = "Ship Destroyed";
    private int finalScore = 0;

    public GameOverPanel(SpaceScreen parentScreen, int playerId, String username) {
        this.parentScreen = parentScreen;
        this.playerId = playerId;
        this.username = username;

        setOpaque(false);
        setLayout(new BorderLayout());

        initializeUI();
        initializeEffects();

        if (autoRespawn) {
            startRespawnCountdown();
        }

        setFocusable(true);
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

    private void initializeUI() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));

        respawnButton = new JButton("Respawn Ship");
        styleButton(respawnButton);
        respawnButton.addActionListener(e -> respawnShip());

        returnButton = new JButton("Return to Navigation");
        styleButton(returnButton);
        returnButton.addActionListener(e -> returnToMainMenu());

        buttonPanel.add(respawnButton);
        buttonPanel.add(returnButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void initializeEffects() {
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

        animationTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                explosionPhase += 0.01f;
                for (DebrisParticle particle : debrisParticles) {
                    particle.update();
                }
                repaint();
            }
        });
        animationTimer.start();
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2d);
        drawParticles(g2d);
        if (explosionPhase < 1.0f) {
            drawExplosion(g2d);
        }
        drawGameOverText(g2d);
    }

    private void drawBackground(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

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

        g2d.setColor(new Color(255, 255, 255, 100));
        Random random = new Random(123);
        for (int i = 0; i < 200; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int size = random.nextInt(3) + 1;
            g2d.fillOval(x, y, size, size);
        }
    }

    private void drawParticles(Graphics2D g2d) {
        for (DebrisParticle particle : debrisParticles) {
            particle.draw(g2d);
        }
    }

    private void drawExplosion(Graphics2D g2d) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        float size = 100 + explosionPhase * 300;
        float alpha = Math.max(0, 1.0f - explosionPhase);

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
                Math.round(centerX - diameter / 2),
                Math.round(centerY - diameter / 2),
                Math.round(diameter),
                Math.round(diameter)
            );
        }

        if (explosionPhase < 0.5f) {
            float waveSize = size * 1.2f;
            float waveAlpha = Math.max(0, 0.5f - explosionPhase) * 0.5f;
            g2d.setColor(new Color(1.0f, 1.0f, 1.0f, waveAlpha));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(
                Math.round(centerX - waveSize / 2),
                Math.round(centerY - waveSize / 2),
                Math.round(waveSize),
                Math.round(waveSize)
            );
        }
    }

    private void drawGameOverText(Graphics2D g2d) {
        int width = getWidth();
        int centerX = width / 2;
        int y = 100;

        Font titleFont = new Font("Arial", Font.BOLD, 48);
        g2d.setFont(titleFont);
        String gameOverText = "SHIP DESTROYED";

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gameOverText);

        g2d.setColor(new Color(100, 0, 0, 100));
        g2d.drawString(gameOverText, centerX - textWidth / 2 + 3, y + 3);
        g2d.setColor(new Color(255, 60, 60));
        g2d.drawString(gameOverText, centerX - textWidth / 2, y);

        y += 60;
        Font causeFont = new Font("Arial", Font.ITALIC, 24);
        g2d.setFont(causeFont);
        g2d.setColor(new Color(200, 200, 200));
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(deathCause);
        g2d.drawString(deathCause, centerX - textWidth / 2, y);

        y += 80;
        Font infoFont = new Font("Arial", Font.PLAIN, 18);
        g2d.setFont(infoFont);
        String playerText = "Pilot: " + username;
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(playerText);
        g2d.drawString(playerText, centerX - textWidth / 2, y);

        if (finalScore > 0) {
            y += 30;
            String scoreText = "Final Score: " + finalScore;
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(scoreText);
            g2d.drawString(scoreText, centerX - textWidth / 2, y);
        }

        if (autoRespawn && respawnCountdown > 0) {
            y += 60;
            String respawnText = "Auto-respawn in " + respawnCountdown + " seconds...";
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(new Color(100, 200, 100));
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(respawnText);
            g2d.drawString(respawnText, centerX - textWidth / 2, y);
        }

        y = getHeight() - 70;
        String hintText = "Press SPACE to respawn or ESC to return";
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(180, 180, 180, 180));
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(hintText);
        g2d.drawString(hintText, centerX - textWidth / 2, y);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 100));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(200, 50));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 140));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 100));
            }
        });
    }

    private void respawnShip() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (respawnTimer != null) {
            respawnTimer.stop();
        }
        if (parentScreen != null) {
            parentScreen.dispose();
        }
        SwingUtilities.invokeLater(() -> new SpaceScreen(null, playerId, username).setVisible(true));
    }

    private void returnToMainMenu() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        if (respawnTimer != null) {
            respawnTimer.stop();
        }
        if (parentScreen != null) {
            parentScreen.dispose();
        }
        Client.returnToShip();
    }

    public void setDeathCause(String cause) {
        this.deathCause = cause;
        repaint();
    }

    public void setFinalScore(int score) {
        this.finalScore = score;
        repaint();
    }

    public void setAutoRespawn(boolean autoRespawn) {
        this.autoRespawn = autoRespawn;
        if (autoRespawn && respawnTimer == null) {
            startRespawnCountdown();
        } else if (!autoRespawn && respawnTimer != null) {
            respawnTimer.stop();
            respawnButton.setText("Respawn Ship");
        }
    }

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

            shape = new Path2D.Float();
            int points = 3 + (int)(Math.random() * 4);
            float[] angles = new float[points];
            float[] distances = new float[points];
            for (int i = 0; i < points; i++) {
                angles[i] = (float)(Math.random() * Math.PI * 2);
                distances[i] = (float)(0.5 + Math.random() * 0.5);
            }
            java.util.Arrays.sort(angles);
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
            vx *= 0.99f;
            vy *= 0.99f;
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
