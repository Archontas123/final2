package com.tavuc.ui.panels;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.tavuc.Client;
import com.tavuc.managers.InputManager;
import com.tavuc.managers.WorldManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import java.util.stream.Collectors;
import java.awt.geom.AffineTransform;



public class GamePanel extends GPanel implements ActionListener, MouseMotionListener, MouseListener {

    private int playerId;
    private String username;
    private int gameId; 
    private Player player;
    private InputManager inputManager;
    private Timer gameLoopTimer;
    // private Map<Integer, Player> otherPlayers = new ConcurrentHashMap<>(); // Managed by WorldManager
    private Timer playerUpdateRequester;
    private WorldManager worldManager; 
    // Rendering of other players was previously disabled which meant a player
    // could not see anyone else walking around on a planet.  Enabling this flag
    // by default allows the GamePanel to draw the players managed by
    // WorldManager.
    private boolean renderOtherPlayers = true; // Flag to control rendering


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
        this.inputManager = InputManager.getInstance();
        this.inputManager.setPlayerTarget(this.player);
        this.inputManager.setControlTarget(InputManager.ControlTargetType.PLAYER);
        
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


        int playerSize = 40;
        int handSize = playerSize / 3;
        double handDistance = playerSize * 0.6;
        double handOffsetAngle = Math.PI / 7;
        int textPadding = 5;
        int arcSize = 10;
        int nametagYOffset = 15;

        java.awt.Font originalFont = g2d.getFont();
        java.awt.Font boldFont = originalFont.deriveFont(java.awt.Font.BOLD);

     
        g2d.setColor(Color.BLUE);
        int playerBodyWorldCenterX = player.getX() + playerSize / 2;
        int playerBodyWorldCenterY = player.getY() + playerSize / 2;

        g2d.fillOval(player.getX(), player.getY(), playerSize, playerSize); 

        double playerAngle = player.getDirection();
        double leftHandAngle = playerAngle - handOffsetAngle;
        int leftHandWorldCenterX = playerBodyWorldCenterX + (int) (handDistance * Math.cos(leftHandAngle));
        int leftHandWorldCenterY = playerBodyWorldCenterY + (int) (handDistance * Math.sin(leftHandAngle));
        g2d.fillOval(leftHandWorldCenterX - handSize / 2, leftHandWorldCenterY - handSize / 2, handSize, handSize);

        double rightHandAngle = playerAngle + handOffsetAngle;
        int rightHandWorldCenterX = playerBodyWorldCenterX + (int) (handDistance * Math.cos(rightHandAngle));
        int rightHandWorldCenterY = playerBodyWorldCenterY + (int) (handDistance * Math.sin(rightHandAngle));
        g2d.fillOval(rightHandWorldCenterX - handSize / 2, rightHandWorldCenterY - handSize / 2, handSize, handSize);

        if (player.getDamageEffect() > 0f) {
            int alpha = (int)(player.getDamageEffect() * 150);
            g2d.setColor(new Color(255, 0, 0, alpha));
            g2d.fillOval(player.getX(), player.getY(), playerSize, playerSize);
            g2d.fillOval(leftHandWorldCenterX - handSize / 2, leftHandWorldCenterY - handSize / 2, handSize, handSize);
            g2d.fillOval(rightHandWorldCenterX - handSize / 2, rightHandWorldCenterY - handSize / 2, handSize, handSize);
        }

        // Draw other players
        if (renderOtherPlayers && worldManager != null) {
            for (Player other : worldManager.getOtherPlayers()) {
                if (other.getPlayerId() == this.playerId) continue;

                other.updateDamageEffect();

                g2d.setColor(Color.RED);
            int otherPlayerBodyWorldCenterX = other.getX() + playerSize / 2;
            int otherPlayerBodyWorldCenterY = other.getY() + playerSize / 2;

            g2d.fillOval(other.getX(), other.getY(), playerSize, playerSize); 

            g2d.setColor(new Color(200, 0, 0)); 
            double otherPlayerAngle = other.getDirection();

            double otherLeftHandAngle = otherPlayerAngle - handOffsetAngle;
            int otherLeftHandWorldCenterX = otherPlayerBodyWorldCenterX + (int) (handDistance * Math.cos(otherLeftHandAngle));
            int otherLeftHandWorldCenterY = otherPlayerBodyWorldCenterY + (int) (handDistance * Math.sin(otherLeftHandAngle));
            g2d.fillOval(otherLeftHandWorldCenterX - handSize / 2, otherLeftHandWorldCenterY - handSize / 2, handSize, handSize);

            double otherRightHandAngle = otherPlayerAngle + handOffsetAngle;
            int otherRightHandWorldCenterX = otherPlayerBodyWorldCenterX + (int) (handDistance * Math.cos(otherRightHandAngle));
            int otherRightHandWorldCenterY = otherPlayerBodyWorldCenterY + (int) (handDistance * Math.sin(otherRightHandAngle));
            g2d.fillOval(otherRightHandWorldCenterX - handSize / 2, otherRightHandWorldCenterY - handSize / 2, handSize, handSize);

            if (other.getDamageEffect() > 0f) {
                int oAlpha = (int)(other.getDamageEffect() * 150);
                g2d.setColor(new Color(255, 0, 0, oAlpha));
                g2d.fillOval(other.getX(), other.getY(), playerSize, playerSize);
                g2d.fillOval(otherLeftHandWorldCenterX - handSize / 2, otherLeftHandWorldCenterY - handSize / 2, handSize, handSize);
                g2d.fillOval(otherRightHandWorldCenterX - handSize / 2, otherRightHandWorldCenterY - handSize / 2, handSize, handSize);
            }
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
            for (Player other : worldManager.getOtherPlayers()) {
                if (other.getPlayerId() == this.playerId) continue;

                g2d.setFont(boldFont);
            String otherPlayerText = other.getUsername();
            FontMetrics otherFm = g2d.getFontMetrics();
            int otherPlayerTextWidth = otherFm.stringWidth(otherPlayerText);
            int otherTextAscent = otherFm.getAscent();
            int otherTextDescent = otherFm.getDescent();
            int otherTextVisualHeight = otherTextAscent + otherTextDescent;

            int otherBgWidth = otherPlayerTextWidth + (2 * textPadding);
            int otherBgHeight = otherTextVisualHeight + (2 * textPadding);

            int otherPlayerBodyWorldCenterX = other.getX() + playerSize / 2; 
            int otherBgX = otherPlayerBodyWorldCenterX - (otherPlayerTextWidth / 2) - textPadding;
            int otherBgY = other.getY() - nametagYOffset - otherBgHeight; 

            g2d.setColor(new Color(50, 50, 50, 180)); 
            g2d.fillRoundRect(otherBgX, otherBgY, otherBgWidth, otherBgHeight, arcSize, arcSize);

            g2d.setColor(Color.WHITE); 
            int otherTextX = otherBgX + textPadding;
            int otherTextY = otherBgY + textPadding + otherTextAscent;
            g2d.drawString(otherPlayerText, otherTextX, otherTextY);
            g2d.setFont(originalFont);
            }
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
    }

    /**
     * Handles the game loop timer events.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        player.update();

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
            attemptPlayerAttack();
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
            System.out.println("[GamePanel] Attacking player " + closest.getPlayerId());
            Client.sendPlayerAttack(player.getPlayerId(), closest.getPlayerId());
        } else {
            System.out.println("[GamePanel] No target in melee range");
        }
    }
}
