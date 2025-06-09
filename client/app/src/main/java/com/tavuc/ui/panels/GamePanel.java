package com.tavuc.ui.panels;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.tavuc.Client;
import com.tavuc.managers.InputManager;
import com.tavuc.managers.WorldManager;
import com.tavuc.weapons.ForceAlignment;
import com.tavuc.weapons.ForcePowers;
import com.tavuc.weapons.WeaponStats;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.ColorType;
import com.tavuc.models.planets.Tile;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import java.util.stream.Collectors;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.InputStream;
import java.io.IOException;

import com.tavuc.ui.components.DamagePopup;
import com.tavuc.ui.effects.DustParticle;
import com.tavuc.ui.effects.SpeedLineParticle;
import com.tavuc.ui.effects.MovementParticle;
import com.tavuc.ui.effects.Particle;



public class GamePanel extends GPanel implements ActionListener, MouseMotionListener, MouseListener {

    private int playerId;
    private String username;
    private int gameId; 
    private Player player;
    private InputManager inputManager;
    private ForcePowers forcePowers;
    private Timer gameLoopTimer;
    // private Map<Integer, Player> otherPlayers = new ConcurrentHashMap<>(); // Managed by WorldManager
    private Timer playerUpdateRequester;
    private WorldManager worldManager; 
    // Rendering of other players was previously disabled which meant a player
    // could not see anyone else walking around on a planet.  Enabling this flag
    // by default allows the GamePanel to draw the players managed by
    // WorldManager.
    private boolean renderOtherPlayers = true; // Flag to control rendering
    // Track time of last melee attack to avoid spamming the server
    private long lastAttackTime = 0;

    private java.awt.image.BufferedImage playerSprite;
    private java.awt.image.BufferedImage[] healthbarSprites = new java.awt.image.BufferedImage[7];


    private int shakeTicks = 0;
    private double shakeStrength = 0;

    // Floating damage numbers
    private final List<DamagePopup> damagePopups = new ArrayList<>();

    // All visual particles (movement, weapons, etc.)
    private final List<Particle> particles = new ArrayList<>();

    // Temporary light sources for weapon flashes and effects
    private final List<com.tavuc.ui.lights.DynamicLight> lights = new ArrayList<>();

    // Quick white screen flash when damaged
    private float damageFlash = 0f;


    /**
     * Constructor for GamePanel
     * @param playerId The ID of the player
     * @param username The username of the player
     * @param gameId The ID of the game
     */
    public GamePanel(int playerId, String username, int gameId) {
        super(); 
        setLayout(null); 
        this.playerId = playerId;
        this.username = username;
        this.gameId = gameId;
        this.player = new Player(playerId, username);
        // Increase melee/force ability range
        this.player.setAttackRange(this.player.getAttackRange() * 4);
        this.inputManager = InputManager.getInstance();
        this.inputManager.setPlayerTarget(this.player);
        this.inputManager.setControlTarget(InputManager.ControlTargetType.PLAYER);
        this.forcePowers = new ForcePowers(
                100,
                ForceAlignment.LIGHT,
                new WeaponStats(1, this.player.getAttackRange(), 1.0)
        );
        this.inputManager.setForcePowers(this.forcePowers);
        System.out.println("Force powers unlocked! Press F1-F3 to use abilities.");

        // Use Client's WorldManager if available, otherwise create a new one
        if (Client.worldManager != null && Client.worldManager.getGameId() == gameId) {
            this.worldManager = Client.worldManager;
            System.out.println("GamePanel: Using existing WorldManager from Client for game ID: " + gameId);
        } else {
            this.worldManager = new WorldManager(gameId);
            Client.worldManager = this.worldManager; // Set it for the Client if we created it
            System.out.println("GamePanel: Initialized new WorldManager for game ID: " + gameId);
        }

        addKeyListener(this.inputManager);
        addMouseMotionListener(this);
        addMouseListener(this);
        setFocusable(true);
        requestFocusInWindow();

        gameLoopTimer = new Timer(16, this);
        gameLoopTimer.start();

        Client.currentGamePanel = this;

        loadSprites();

        try {
            Client.requestPlanetPalette(this.gameId);
        } catch (Exception e) {
            System.err.println("GamePanel: Error requesting initial planet palette: " + e.getMessage());
        }

        /*
        playerUpdateRequester = new Timer(2000, e -> {
            try {
                String response = Client.requestPlayers(this.gameId);
                if (response != null && response.startsWith("PLAYERS_DATA")) {
                    // this.processServerMessage(response); // This logic is being moved/made redundant
                } else if (response != null && response.startsWith("ERROR")) {
                    System.err.println("GamePanel Timer: Error response from requestPlayers: " + response);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                System.err.println("GamePanel Timer: Error requesting player updates: " + ex.getMessage());
            }
        });
        playerUpdateRequester.start();
        */
    }

    /**
     * Stops the game loop and cleans up resources.
     */
    public void stopGame() {
        if (gameLoopTimer != null) gameLoopTimer.stop();
        // if (playerUpdateRequester != null) playerUpdateRequester.stop(); // Already commented out
        Client.currentGamePanel = null; 
    }

    // The methods processPlayerUpdate and processServerMessage are removed
    // as their functionalities are now handled by WorldManager via Client.java

    /**
     * Returns the player ID.
     * @return The player ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Returns the username.
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the game ID.
     * @return The game ID
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Paints the game panel.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK); 
        g2d.fillRect(0, 0, getWidth(), getHeight());

       
        int playerVisualX = player.getX(); 
        int playerVisualY = player.getY();

        int translateX = getWidth() / 2 - playerVisualX;
        int translateY = getHeight() / 2 - playerVisualY;
        if (shakeTicks > 0) {
            translateX += (int)((Math.random()-0.5) * shakeStrength);
            translateY += (int)((Math.random()-0.5) * shakeStrength);
            shakeTicks--;
            if (shakeTicks == 0) shakeStrength = 0;
        }

        AffineTransform originalTransform = g2d.getTransform();
        g2d.translate(translateX, translateY);

        if (worldManager != null) {
            ColorPallete palette = worldManager.getCurrentPalette(); 
            if (palette != null) {
                for (Chunk chunk : worldManager.getChunks().values()) {
                    if (chunk == null) continue;
                    Tile[][] tiles = chunk.getTiles();
                    for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                        for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                            Tile tile = tiles[x][y];
                            if (tile != null) {
                                Color tileColor = getColorForTile(tile.getColorType(), palette);
                                g2d.setColor(tileColor);
                                g2d.fillRect(tile.getX() * WorldManager.TILE_SIZE,
                                             tile.getY() * WorldManager.TILE_SIZE,
                                             WorldManager.TILE_SIZE, WorldManager.TILE_SIZE);
                                g2d.setColor(tileColor.darker()); 
                                g2d.drawRect(tile.getX() * WorldManager.TILE_SIZE,
                                             tile.getY() * WorldManager.TILE_SIZE,
                                             WorldManager.TILE_SIZE, WorldManager.TILE_SIZE);
                            }
                        }
                    }
                }
            } else {
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString("Palette not loaded...", player.getX() - 50, player.getY() - 50);
            }
        }

        // Draw coin drops
        if (worldManager != null) {
            g2d.setColor(Color.YELLOW);
            for (com.tavuc.models.items.CoinDrop drop : worldManager.getCoinDrops()) {
                g2d.fillOval(drop.getX(), drop.getY(), drop.getWidth(), drop.getHeight());
            }
        }

        int playerSize = 40;
        int handSize = playerSize / 3; // unused now but kept for compatibility
        double handDistance = playerSize * 0.6; // unused
        double handOffsetAngle = Math.PI / 7; // unused
        int textPadding = 5;
        int arcSize = 10;
        int nametagYOffset = 15;

        java.awt.Font originalFont = g2d.getFont();
        java.awt.Font boldFont = originalFont.deriveFont(java.awt.Font.BOLD);

     
        int playerBodyWorldCenterX = player.getX() + playerSize / 2;
        int playerBodyWorldCenterY = player.getY() + playerSize / 2;

        if (playerSprite != null) {
            g2d.drawImage(playerSprite, player.getX(), player.getY(), playerSize, playerSize, null);
        } else {
            g2d.setColor(Color.BLUE);
            g2d.fillOval(player.getX(), player.getY(), playerSize, playerSize);
        }

        if (player.getDamageEffect() > 0f) {
            int alpha = (int)(player.getDamageEffect() * 150);
            g2d.setColor(new Color(255, 0, 0, alpha));
            g2d.fillOval(player.getX(), player.getY(), playerSize, playerSize);
        }

        // Draw particles relative to world transform
        for (Particle p : particles) {
            p.draw(g2d, 0, 0);
        }

        // Draw other players
        if (renderOtherPlayers && worldManager != null) {
            double dt = 1.0 / 60.0; // assume 60 FPS
            for (Player other : worldManager.getOtherPlayers()) {
                if (other.getPlayerId() == this.playerId) continue;

                other.updateDamageEffect();

                // Predict next movement delta based on last velocity
                com.tavuc.utils.Vector2D pd = other.getMovementController().predictNextDelta(dt);
                double predictedX = other.getX() + pd.getX();
                double predictedY = other.getY() + pd.getY();

                // Blend predicted and server positions
                double blend = 0.5;
                double renderX = other.getX() * (1 - blend) + predictedX * blend;
                double renderY = other.getY() * (1 - blend) + predictedY * blend;

                g2d.setColor(Color.RED);
                if (playerSprite != null) {
                    g2d.drawImage(playerSprite, (int)renderX, (int)renderY, playerSize, playerSize, null);
                } else {
                    g2d.fillOval((int)renderX, (int)renderY, playerSize, playerSize);
                }

                if (other.getDamageEffect() > 0f) {
                    int oAlpha = (int)(other.getDamageEffect() * 150);
                    g2d.setColor(new Color(255, 0, 0, oAlpha));
                    g2d.fillOval((int)renderX, (int)renderY, playerSize, playerSize);
                }
            }
        }

        // Draw floating damage numbers while world transform is active
        Iterator<DamagePopup> iter = damagePopups.iterator();
        while (iter.hasNext()) {
            DamagePopup dp = iter.next();
            if (dp.update()) {
                iter.remove();
            } else {
                dp.draw(g2d, 0, 0);
            }
        }


        g2d.setFont(boldFont);
        String playerText = player.getUsername() + " (You)";
        FontMetrics fm = g2d.getFontMetrics();
        int playerTextWidth = fm.stringWidth(playerText);
        int textAscent = fm.getAscent();
        int textDescent = fm.getDescent();
        int textVisualHeight = textAscent + textDescent;

        int margin = textPadding;

        int playerBgWidth = playerTextWidth + (2 * margin);
        int playerBgHeight = textVisualHeight + (2 * margin);

        int playerBgX = playerBodyWorldCenterX - (playerTextWidth / 2) - margin; 
        int playerBgY = player.getY() - nametagYOffset - playerBgHeight; 

        g2d.setColor(new Color(0, 0, 0, 180)); 
        g2d.fillRoundRect(playerBgX, playerBgY, playerBgWidth, playerBgHeight, arcSize, arcSize);

        g2d.setColor(Color.WHITE); 
        int playerTextX = playerBgX + margin;
        int playerTextY = playerBgY + margin + textAscent;
        g2d.drawString(playerText, playerTextX, playerTextY);
        g2d.setFont(originalFont);

        // Draw other player names
        if (renderOtherPlayers && worldManager != null) {
            double dt = 1.0 / 60.0;
            for (Player other : worldManager.getOtherPlayers()) {
                if (other.getPlayerId() == this.playerId) continue;

                // Predict and blend positions same as sprite rendering
                com.tavuc.utils.Vector2D pd = other.getMovementController().predictNextDelta(dt);
                double predictedX = other.getX() + pd.getX();
                double predictedY = other.getY() + pd.getY();
                double blend = 0.5;
                double renderX = other.getX() * (1 - blend) + predictedX * blend;
                double renderY = other.getY() * (1 - blend) + predictedY * blend;

                g2d.setFont(boldFont);
                String otherPlayerText = other.getUsername();
                FontMetrics otherFm = g2d.getFontMetrics();
                int otherPlayerTextWidth = otherFm.stringWidth(otherPlayerText);
                int otherTextAscent = otherFm.getAscent();
                int otherTextDescent = otherFm.getDescent();
                int otherTextVisualHeight = otherTextAscent + otherTextDescent;

                int otherBgWidth = otherPlayerTextWidth + (2 * textPadding);
                int otherBgHeight = otherTextVisualHeight + (2 * textPadding);

                int otherPlayerBodyWorldCenterX = (int)renderX + playerSize / 2;
                int otherBgX = otherPlayerBodyWorldCenterX - (otherPlayerTextWidth / 2) - textPadding;
                int otherBgY = (int)renderY - nametagYOffset - otherBgHeight;

                g2d.setColor(new Color(50, 50, 50, 180));
                g2d.fillRoundRect(otherBgX, otherBgY, otherBgWidth, otherBgHeight, arcSize, arcSize);

                g2d.setColor(Color.WHITE);
                int otherTextX = otherBgX + textPadding;
                int otherTextY = otherBgY + textPadding + otherTextAscent;
                g2d.drawString(otherPlayerText, otherTextX, otherTextY);
                g2d.setFont(originalFont);
            }
        }

        // Draw transient light sources
        for (com.tavuc.ui.lights.DynamicLight l : lights) {
            l.draw(g2d, 0, 0);
        }

        g2d.setTransform(originalTransform);

        // --- Fog of war / lighting effect ---
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        Point2D center = new Point2D.Float(panelWidth / 2f, panelHeight / 2f);
        float radius = Math.max(panelWidth, panelHeight) * 0.6f;
        float[] dist = {0f, 0.4f, 1f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 150),
            new Color(0, 0, 0, 220)
        };
        RadialGradientPaint fog = new RadialGradientPaint(center, radius, dist, colors);
        g2d.setPaint(fog);
        g2d.fillRect(0, 0, panelWidth, panelHeight);

        // Health-based vignette darkening the edges
        float healthRatio = player.getHealth() / 6f;
        float vignetteAlpha = Math.max(0f, 1f - healthRatio);
        if (vignetteAlpha > 0f) {
            float[] vd = {0.5f, 1f};
            Color[] vc = {
                new Color(0, 0, 0, 0),
                new Color(80, 0, 0, (int)(vignetteAlpha * 180))
            };
            RadialGradientPaint vignette = new RadialGradientPaint(center, radius, vd, vc);
            g2d.setPaint(vignette);
            g2d.fillRect(0, 0, panelWidth, panelHeight);
        }

        // Draw player health bar above the lighting effect
        int healthIdx = Math.max(0, Math.min(6, player.getHealth()));
        if (healthbarSprites[healthIdx] != null) {
            int scale = 2; // slightly smaller than before
            int padding = 15;
            int width = healthbarSprites[healthIdx].getWidth();
            int height = healthbarSprites[healthIdx].getHeight();
            g2d.drawImage(
                healthbarSprites[healthIdx],
                padding,
                padding,
                width * scale,
                height * scale,
                null
            );
        }

        // Display current coin count in the top-right corner
        g2d.setColor(Color.YELLOW);
        String coinText = "Coins: " + player.getCoins();
        FontMetrics fmCoins = g2d.getFontMetrics();
        int coinX = panelWidth - fmCoins.stringWidth(coinText) - 20;
        int coinY = 20 + fmCoins.getAscent();
        g2d.drawString(coinText, coinX, coinY);

        // White flash overlay when taking damage
        if (damageFlash > 0f) {
            g2d.setColor(new Color(255, 255, 255, (int)(damageFlash * 180)));
            g2d.fillRect(0, 0, panelWidth, panelHeight);
        }

        // Mana and ability display removed
    }

    /**
     * Handles the game loop timer events.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        player.update();

        updateEffects();

        if (worldManager != null) {
          
            worldManager.updateVisibleChunks(player.getX(), player.getY(), getWidth(), getHeight());
        }

        boolean hasMoved = player.getX() != player.getLastSentX() ||
                           player.getY() != player.getLastSentY() ||
                           player.getDx() != player.getLastSentDx() ||
                           player.getDy() != player.getLastSentDy() ||
                           player.getDirection() != player.getlastSentDirection();

        if (hasMoved) {
            Client.sendPlayerUpdate(player.getPlayerId(), player.getX(), player.getY(), player.getDx(), player.getDy(), player.getDirection());
            player.setLastSentX(player.getX());
            player.setLastSentY(player.getY());
            player.setLastSentDx(player.getDx());
            player.setLastSentDy(player.getDy());
            player.setlastSentDirection(player.getDirection());
        }

        if (inputManager.isKeyPressed(java.awt.event.KeyEvent.VK_Q)) {
            long now = System.currentTimeMillis();
            if (now - lastAttackTime > 500) {
                System.out.println("[GamePanel] Q pressed - attempting melee attack");
                attemptPlayerAttack();
                lastAttackTime = now;
            } else {
                System.out.println("[GamePanel] Attack on cooldown");
            }
        }

        repaint();
    }



    /**
     * Handles mouse dragged events.
     * @param e The mouse event
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        updatePlayerDirection(e);
    }

    /**
     * Handles mouse moved events.
     * @param e The mouse event
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        updatePlayerDirection(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Currently unused - melee is triggered with the Q key
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    /**
     * Updates the player's direction based on mouse movement.
     * @param e The mouse event
     */
    private void updatePlayerDirection(MouseEvent e) {
        if (player == null) return;

        double screenCenterX = getWidth() / 2.0;
        double screenCenterY = getHeight() / 2.0;

        double mouseX = e.getX();
        double mouseY = e.getY();

        double angle = Math.atan2(mouseY - screenCenterY, mouseX - screenCenterX);
        player.setDirection(angle);
    }

    /**
     * Returns the color for a tile based on its type and the current palette.
     * @param colorType The color type of the tile
     * @param palette The current color palette
     * @return The color for the tile
     */
    private Color getColorForTile(ColorType colorType, ColorPallete palette) {
        if (palette == null || colorType == null) {
            return Color.MAGENTA;
        }
        switch (colorType) {
            case PRIMARY_SURFACE: return palette.getPrimarySurface();
            case PRIMARY_LIQUID: return palette.getPrimaryLiquid();
            case SECONDARY_SURFACE: return palette.getSecondarySurface();
            case TERTIARY_SURFACE: return palette.getTertiarySurface();
            case HUE_SHIFT: return palette.getHueShift();
            case ROCK: return palette.getRock();
            default: return Color.GRAY;
        }
    }

    /** Adds a floating damage popup at the specified world position. */
    private void addDamagePopup(double worldX, double worldY, double damage) {
        String text = String.format("%.1f", damage);
        Color c = (damage == 0) ? new Color(80, 160, 220) : new Color(220, 60, 40);
        damagePopups.add(new DamagePopup(worldX, worldY, text, c));
    }

    /** Triggered by network events to display damage on a player. */
    public void showPlayerDamage(int id, double damage) {
        Player target = null;
        if (id == this.playerId) {
            target = player;
        } else if (worldManager != null) {
            target = worldManager.getOtherPlayer(id);
        }
        if (target != null) {
            addDamagePopup(target.getX() + target.getWidth() / 2.0, target.getY(), damage);
            if (id == this.playerId) {
                triggerDamageFlash();
            }
        }
    }

    /**
     * Attempts a melee attack on the closest player within range.
     */
    private void attemptPlayerAttack() {
        if (worldManager == null || player == null) return;

        Player closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Player other : worldManager.getOtherPlayers()) {
            double dx = other.getX() - player.getX();
            double dy = other.getY() - player.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < closestDist) {
                closestDist = dist;
                closest = other;
            }
        }

        double range = player.getAttackRange();
        if (closest != null && closestDist <= range) {
            Client.sendPlayerAttack(player.getPlayerId(), closest.getPlayerId());
        }
    }

    /** Update and spawn visual particles based on player state. */
    private void updateEffects() {
        com.tavuc.controllers.MovementState state = player.getMovementController().getCurrentState();
        if (state == com.tavuc.controllers.MovementState.SLIDING) {
            particles.add(new DustParticle(player.getX() + player.getWidth() / 2.0,
                                           player.getY() + player.getHeight()));
        } else if (state == com.tavuc.controllers.MovementState.DODGING) {
            particles.add(new SpeedLineParticle(player.getX() + player.getWidth() / 2.0,
                                               player.getY() + player.getHeight() / 2.0,
                                               player.getDirection()));
        }

        Iterator<Particle> iterP = particles.iterator();
        while (iterP.hasNext()) {
            Particle p = iterP.next();
            if (p.update()) {
                iterP.remove();
            }
        }

        Iterator<com.tavuc.ui.lights.DynamicLight> itL = lights.iterator();
        while (itL.hasNext()) {
            com.tavuc.ui.lights.DynamicLight l = itL.next();
            if (l.update()) {
                itL.remove();
            }
        }

        if (damageFlash > 0f) {
            damageFlash = Math.max(0f, damageFlash - 0.1f);
        }
    }

    private void loadSprites() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("assets/player/base_main_pixel.png")) {
            if (is != null) {
                playerSprite = ImageIO.read(is);
            }
        } catch (IOException e) {
            System.err.println("Failed to load player sprite: " + e.getMessage());
        }

        for (int i = 0; i <= 6; i++) {
            String path = "assets/healthbar/healthbar_" + i + "-6.png";
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    healthbarSprites[i] = ImageIO.read(is);
                }
            } catch (IOException e) {
                System.err.println("Failed to load healthbar sprite " + path + ": " + e.getMessage());
            }
        }

        // Mana bar sprites were part of the old ability system; nothing to load now
    }

    /** Trigger small screen shake for visual feedback. */
    public void triggerScreenShake(int ticks, double strength) {
        this.shakeTicks = Math.max(this.shakeTicks, ticks);
        this.shakeStrength = Math.max(this.shakeStrength, strength);
    }

    /** Add a particle effect to be managed by the panel. */
    public void addParticle(Particle p) {
        particles.add(p);
    }

    /** Add a light effect to be managed by the panel. */
    public void addLight(com.tavuc.ui.lights.DynamicLight l) {
        lights.add(l);
    }

    /** Trigger a brief white flash when the player is hurt. */
    public void triggerDamageFlash() {
        damageFlash = 1f;
    }

}
