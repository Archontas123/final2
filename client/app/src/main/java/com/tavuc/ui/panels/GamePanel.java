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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import java.util.stream.Collectors;
import java.awt.geom.AffineTransform;



public class GamePanel extends GPanel implements ActionListener, MouseMotionListener {

    private int playerId;
    private String username;
    private int gameId; 
    private Player player;
    private InputManager inputManager;
    private Timer gameLoopTimer;
    private Map<Integer, Player> otherPlayers = new ConcurrentHashMap<>();
    private Timer playerUpdateRequester;
    private WorldManager worldManager; 


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
        this.worldManager = new WorldManager(gameId); 
        Client.worldManager = this.worldManager; 

        addKeyListener(this.inputManager);
        addMouseMotionListener(this);
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


        playerUpdateRequester = new Timer(2000, e -> {
            try {
                String response = Client.requestPlayers(this.gameId);
                if (response != null && response.startsWith("PLAYERS_DATA")) {
                    this.processServerMessage(response);
                } else if (response != null && response.startsWith("ERROR")) {
                    System.err.println("GamePanel Timer: Error response from requestPlayers: " + response);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                System.err.println("GamePanel Timer: Error requesting player updates: " + ex.getMessage());
            }
        });
        playerUpdateRequester.start();
    }

    /**
     * Stops the game loop and cleans up resources.
     */
    public void stopGame() {
        if (gameLoopTimer != null) gameLoopTimer.stop();
        if (playerUpdateRequester != null) playerUpdateRequester.stop();
        Client.currentGamePanel = null; 
    }

    /**
     * Processes messages received from the server.
     * @param message The message from the server
     */
    public void processPlayerUpdate(com.tavuc.networking.models.PlayerUpdateBroadcast event) {
        if (event.playerId.equals(String.valueOf(this.playerId))) {
            return; 
        }
        Player otherP = otherPlayers.computeIfAbsent(Integer.parseInt(event.playerId), id -> new Player(id, "Player" + id)); 
        otherP.setX((int)event.x);
        otherP.setY((int)event.y);
        otherP.setDx(event.dx);
        otherP.setDy(event.dy);
        otherP.setDirection(event.directionAngle);
        repaint();
    }

    public void processServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = message.split(" ", 3); 

            if (parts.length < 1) return;
            String command = parts[0];

            if ("PLAYERS_DATA".equals(command)) {
                if (parts.length < 3) return; 
                String playersInfo = parts[2].trim(); 

                Map<Integer, Player> currentUpdatePlayers = new ConcurrentHashMap<>();
                if (playersInfo.isEmpty() || playersInfo.equals(" ") || playersInfo.equals("null")) { 
                    System.out.println("PlayersInfo is empty, clearing otherPlayers.");
                } else {
                    String[] individualPlayersData = playersInfo.split(";");
                    System.out.println("Split individualPlayersData count: " + individualPlayersData.length);

                    for (String playerDataString : individualPlayersData) {
                        System.out.println("Processing playerDataString: '" + playerDataString + "'");
                        String[] pData = playerDataString.split(":");
                        if (pData.length == 7) { 
                            try {
                                int pId = Integer.parseInt(pData[0]);
                                String pUsername = pData[1];
                                int pX = Integer.parseInt(pData[2]);
                                int pY = Integer.parseInt(pData[3]);
                                double pDx = Double.parseDouble(pData[4]);
                                double pDy = Double.parseDouble(pData[5]);
                                double pDirection = Double.parseDouble(pData[6]);

                                if (pId == this.playerId) {
      
                                    System.out.println("Skipping self in PLAYERS_DATA for main attributes: " + pUsername + " (ID: " + pId + ")");
                                  
                                    continue; 
                                }

                                Player otherP = new Player(pId, pUsername); 
                                System.out.println("Processing other player from data: " + pUsername + " (ID: " + pId + ")");
                                
                                otherP.setX(pX);
                                otherP.setY(pY);
                                otherP.setDx(pDx);
                                otherP.setDy(pDy);
                                otherP.setDirection(pDirection);
                                currentUpdatePlayers.put(pId, otherP);
                                System.out.println("Added/Updated to currentUpdatePlayers: " + pUsername + " (ID: " + pId + ") with angle " + pDirection);
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing player data entry: '" + playerDataString + "' - " + e.getMessage());
                            }
                        } else {
                            System.err.println("Invalid pData length for entry '" + playerDataString + "'. Expected 7, Got: " + pData.length);
                        }
                    }
                }
                otherPlayers.clear(); 
                otherPlayers.putAll(currentUpdatePlayers);
                System.out.println("Final otherPlayers map size: " + otherPlayers.size());
                if (!otherPlayers.isEmpty()) {
                    System.out.println("Other players: " + otherPlayers.values().stream().map(p -> p.getUsername()+"("+p.getPlayerId()+")").collect(Collectors.joining(", ")));
                }


            } else if ("PLAYER_MOVED".equals(command)) {
                System.out.println("GamePanel processing PLAYER_MOVED: " + message);
     
                String actualMoveDataString = message.substring(command.length() + 1).trim(); 
                String[] moveData = actualMoveDataString.split(" "); 
                if (moveData.length == 6) { 
                    try {
                        int pId = Integer.parseInt(moveData[0]);
                        if (pId == this.playerId) return; 

                        Player movedPlayer = otherPlayers.get(pId);
                        if (movedPlayer != null) {
                            movedPlayer.setX(Integer.parseInt(moveData[1]));
                            movedPlayer.setY(Integer.parseInt(moveData[2]));
                            movedPlayer.setDx(Double.parseDouble(moveData[3]));
                            movedPlayer.setDy(Double.parseDouble(moveData[4]));
                            movedPlayer.setDirection(Double.parseDouble(moveData[5]));
                        } else {
                            System.out.println("PLAYER_MOVED for unknown player ID: " + pId + ". Requesting full player list.");
                            try {
                                String fullPlayerData = Client.requestPlayers(this.gameId);
                                if (fullPlayerData != null && fullPlayerData.startsWith("PLAYERS_DATA")) {
                              
                                    this.processServerMessage(fullPlayerData); 
                                }
                            } catch (Exception ex) {
                                System.err.println("Error requesting players after unknown PLAYER_MOVED: " + ex.getMessage());
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing PLAYER_MOVED data items: '" + actualMoveDataString + "' - " + e.getMessage());
                    }
                } else {
                    System.err.println("Invalid PLAYER_MOVED data items. Expected 6, got " + moveData.length + " from '" + actualMoveDataString + "' in message: " + message);
                }
            }
          

            repaint(); 
        });
    }

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

        for (Player other : otherPlayers.values()) {
            if (other.getPlayerId() == this.playerId) continue;

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

        for (Player other : otherPlayers.values()) {
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

        g2d.setTransform(originalTransform);
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
}
