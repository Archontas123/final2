package com.tavuc.ui.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.tavuc.managers.InputManager;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Tile;
import com.tavuc.models.planets.ColorType; 

public class ShipInteriorPanel extends GPanel implements ActionListener {

    private int playerId;
    private String username;
    private Player player;
    private InputManager inputManager;
    private Tile[][] shipLayout;

    private static final int MAP_WIDTH = 50;
    private static final int MAP_HEIGHT = 25;
    public static final int TILE_SIZE = 32; 

    public ShipInteriorPanel(int playerId, String username) {
        super();
        this.playerId = playerId;
        this.username = username;
        this.player = new Player(playerId, username); 
        
        this.inputManager = InputManager.getInstance();
        this.inputManager.setPlayerTarget(this.player); 
        this.inputManager.setControlTarget(InputManager.ControlTargetType.PLAYER); 

        addKeyListener(this.inputManager);
        setFocusable(true);
        requestFocusInWindow();

        initializeShipLayout();
    }

    private void initializeShipLayout() {
        shipLayout = new Tile[MAP_WIDTH][MAP_HEIGHT];
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                if (x == 0 || x == MAP_WIDTH - 1 || y == 0 || y == MAP_HEIGHT - 1) {
                    shipLayout[x][y] = new Tile(x, y, "SHIP_WALL", ColorType.SHIP_WALL);
                } else {
                    shipLayout[x][y] = new Tile(x, y, "SHIP_FLOOR", ColorType.SHIP_FLOOR);
                }
            }
        }
        player.setX(MAP_WIDTH / 2 * TILE_SIZE);
        player.setY(MAP_HEIGHT / 2 * TILE_SIZE);
    }

    public void update() {
        player.update(); 

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        AffineTransform originalTransform = g2d.getTransform();
        
        int playerVisualX = player.getX();
        int playerVisualY = player.getY();
        int translateX = getWidth() / 2 - playerVisualX;
        int translateY = getHeight() / 2 - playerVisualY;
        g2d.translate(translateX, translateY);

        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                Tile tile = shipLayout[x][y];
                if (tile != null) {
                    g2d.setColor(getTileColor(tile.getColorType()));
                    g2d.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    g2d.setColor(Color.BLACK); 
                    g2d.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        g2d.setColor(Color.CYAN);
        g2d.fillOval(player.getX() - TILE_SIZE / 4, player.getY() - TILE_SIZE / 4, TILE_SIZE / 2, TILE_SIZE / 2);

        g2d.setTransform(originalTransform);
    }

    private Color getTileColor(ColorType colorType) {
      
        if (colorType == null) return Color.MAGENTA; 
        switch (colorType) {
            case SHIP_FLOOR: return new Color(180, 180, 180);
            case SHIP_WALL: return new Color(100, 100, 100);  
            case PRIMARY_SURFACE: return Color.LIGHT_GRAY; 
            case ROCK: return Color.GRAY; 
            default: return Color.PINK; 
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
