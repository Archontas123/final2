package com.tavuc.ui.panels;

import javax.swing.*;

import com.tavuc.Client;
import com.tavuc.managers.SpaceManager;
import com.tavuc.managers.InputManager;
import com.tavuc.models.space.Moon;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;

import com.tavuc.ui.screens.GScreen;
import com.tavuc.ui.screens.GameScreen;
import com.tavuc.ui.screens.SpaceScreen;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tavuc.networking.models.JoinGameResponse;
import java.util.List;
import java.util.Map;
import java.util.HashMap; 
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SpacePanel extends GPanel implements KeyListener, MouseListener, ActionListener, ISpacePanel {

    private static class StarData {
        Point worldPos;
        int size;
        Color color;

        StarData(int x, int y, int size, Color color) {
            this.worldPos = new Point(x, y);
            this.size = size;
            this.color = color;
        }
    }

    private static class FireEffect {
        String attackShipId;
        int fromX, fromY, toX, toY;
        long startTime;
        long duration = 500;
        
        FireEffect(String attackShipId, int fromX, int fromY, int toX, int toY) {
            this.attackShipId = attackShipId;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.startTime = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - startTime > duration;
        }
    }

    private Ship playerShip;
    private SpaceManager spaceManager;
    private InputManager inputManager;
    private Timer gameLoopTimer;
    private SpaceScreen parentScreen;
    private int playerId;
    private String username;
    private JFrame mainFrame; 
    private static final int PANEL_WIDTH = 800; 
    private static final int PANEL_HEIGHT = 600;
    private static final int WORLD_WIDTH = PANEL_WIDTH * 5;
    private static final int WORLD_HEIGHT = PANEL_HEIGHT * 5;
    private static final double PLAYER_SPEED = 5.0; 
    private static final double PROXIMITY_THRESHOLD = 40.0;
    private List<StarData> starField;
    private static final int NUM_STARS = 200;
    private Map<Integer, Ship> otherPlayerShips;
    private static final Gson gson = new Gson();
    


    private double lastFetchGalaxyX = 0;
    private double lastFetchGalaxyY = 0;
    private volatile boolean isFetchingPlanets = false;
    private static final double FETCH_RADIUS = 1500;
    private static final double FETCH_TRIGGER_DISTANCE_FACTOR = 0.75;

    private static final double ARROW_VISIBILITY_RANGE = 2000.0; 
    private static final int ARROW_SIZE = 15;
    private static final Color ARROW_COLOR = Color.GREEN;

    public SpacePanel(SpaceScreen parentScreen, JFrame mainFrame, int playerId, String username, SpaceManager spaceManager) {
        super(); 
        this.parentScreen = parentScreen; 
        this.mainFrame = mainFrame;
        this.playerId = playerId;
        this.username = username;
        this.spaceManager = spaceManager;

        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setOpaque(true); 
        setBackground(Color.BLACK);

        this.playerShip = new Ship(PANEL_WIDTH / 2.0, PANEL_HEIGHT / 2.0);
        this.spaceManager.addShip(this.playerShip); 
        
        this.inputManager = InputManager.getInstance();
        this.inputManager.setShipTarget(this.playerShip);
        this.inputManager.setControlTarget(InputManager.ControlTargetType.SHIP);

        addKeyListener(this.inputManager);
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (parentScreen instanceof GScreen) {
                    ((GScreen)parentScreen).reportMouseMovedOnContent(e.getPoint());
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();

        this.gameLoopTimer = new Timer(16, this); 
        this.gameLoopTimer.start();
        this.otherPlayerShips = new HashMap<>();
        
  
        
        initializeStars();
        fetchInitialSystems();
    
    }

    private void fetchInitialSystems() {
        final double initialCenterX = 0;
        final double initialCenterY = 0;
        final double initialRadius = 1000;

        System.out.println("SpacePanel: Fetching initial planets (async)...");
        CompletableFuture.supplyAsync(() -> {
            try {
                return Client.requestPlanetsArea(initialCenterX, initialCenterY, initialRadius);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                System.err.println("SpacePanel: Error fetching initial planets in background: " + e.getMessage());
                e.printStackTrace(); // Keep this for debugging background errors
                return null;
            }
        }).thenAccept(planetsData -> {
            SwingUtilities.invokeLater(() -> {
                if (planetsData != null && planetsData.trim().startsWith("{") && planetsData.trim().endsWith("}")) {
                    spaceManager.parseAndStorePlanetsData(planetsData);
                    System.out.println("SpacePanel: Initial planets data received and parsed.");
                } else {
                    System.err.println("SpacePanel: Failed to fetch initial planets or no valid JSON data (async): " + planetsData);
                }
                repaint(); // Repaint after processing initial data
            });
        });
    }

    private void initializeStars() {
        starField = new ArrayList<>();
        Random random = new Random();
        Color[] starColors = {
            Color.WHITE, 
            Color.LIGHT_GRAY, 
            new Color(255, 255, 220), 
            new Color(220, 235, 255)  
        };

        for (int i = 0; i < NUM_STARS; i++) {
            int x = random.nextInt(WORLD_WIDTH) - WORLD_WIDTH / 2;
            int y = random.nextInt(WORLD_HEIGHT) - WORLD_HEIGHT / 2;
            
            int size = random.nextInt(4) + 2; 
            Color color = starColors[random.nextInt(starColors.length)];
            
            starField.add(new StarData(x, y, size, color));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawStars(g2d);

        AffineTransform originalTransform = g2d.getTransform();
  
        double camX = (playerShip != null) ? playerShip.getX() : 0; 
        double camY = (playerShip != null) ? playerShip.getY() : 0;

        g2d.translate(getWidth() / 2.0 - camX, getHeight() / 2.0 - camY);

        if (spaceManager != null && spaceManager.getSpace() != null) {
            for (Planet planet : spaceManager.getLoadedPlanets()) {
                planet.draw(g2d); 

            }
        }

        if (playerShip != null && Client.currentSpacePanel == this) {
            playerShip.draw(g2d);
        }

        for (Ship otherShip : otherPlayerShips.values()) {
            otherShip.draw(g2d);
        }


        g2d.setTransform(originalTransform);

        drawPlanetArrows(g2d, camX, camY); 
    }

  
    private void drawStars(Graphics2D g2d) {
        double parallaxFactor = 0.1;

        for (StarData star : starField) { 
            AffineTransform starTransform = new AffineTransform();
            if (playerShip != null && Client.currentSpacePanel == this) {
                starTransform.translate(
                    (getWidth() / 2.0 - playerShip.getX()) * parallaxFactor, 
                    (getHeight() / 2.0 - playerShip.getY()) * parallaxFactor
                );
            }
            
            Point2D.Double worldP = new Point2D.Double(star.worldPos.x, star.worldPos.y);
            Point2D.Double screenP = new Point2D.Double();
            starTransform.transform(worldP, screenP);

            if (screenP.x >= -50 && screenP.x <= getWidth() + 50 && 
                screenP.y >= -50 && screenP.y <= getHeight() + 50) {
                
                g2d.setColor(star.color); 
                g2d.fillOval((int)screenP.x, (int)screenP.y, star.size, star.size); 
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) { 
        if (e.getSource() == gameLoopTimer) {
            gameUpdate();
        }
    }

    private void gameUpdate() {
        if (playerShip == null) return;


        spaceManager.tick(); 
    
        sendPlayerShipData();

        if (parentScreen != null && playerShip != null) {
            parentScreen.updatePlayerCoordinatesOnUI(playerShip.getX(), playerShip.getY());
        }

        if (spaceManager != null && spaceManager.getSpace() != null) {
            final double MOON_ANGULAR_VELOCITY = 0.002; 

            for (Planet planet : spaceManager.getLoadedPlanets()) {
                planet.isNear(playerShip, PROXIMITY_THRESHOLD);


            }
        }

        checkCollisions();
        fetchMorePlanetsIfNeeded(); 


        repaint();
    }

    private void sendPlayerShipData() {
        if (playerShip != null && Client.currentSpacePanel == this) {
            Client.sendShipUpdate(playerId, playerShip.getX(), playerShip.getY(), playerShip.getAngle(), playerShip.getDx(), playerShip.getDy(), playerShip.isThrusting());
        }
    }

    public void updateOtherShip(int otherPlayerId, double x, double y, double angle, double dx, double dy, boolean thrusting) {
        if (otherPlayerId == this.playerId) {
            return; 
        }
        Ship ship = otherPlayerShips.get(otherPlayerId);
        if (ship == null) {
            ship = new Ship(x, y); 
            otherPlayerShips.put(otherPlayerId, ship);
        }
        ship.setX(x);
        ship.setY(y);
        ship.setAngle(angle);
        ship.setDx(dx);
        ship.setDy(dy);
        ship.setThrusting(thrusting); 
    }
    
    public void removeOtherShip(int otherPlayerId) {
        otherPlayerShips.remove(otherPlayerId);
    }


  

    private void checkCollisions() {
        if (playerShip == null) return;
    }
    
    private void fetchMorePlanetsIfNeeded() {
        if (playerShip == null || spaceManager == null || isFetchingPlanets) {
            return;
        }

        final double currentShipX = playerShip.getX();
        final double currentShipY = playerShip.getY();

        double distanceToLastFetchCenter = Math.sqrt(
            Math.pow(currentShipX - lastFetchGalaxyX, 2) + Math.pow(currentShipY - lastFetchGalaxyY, 2)
        );

        if (distanceToLastFetchCenter > FETCH_RADIUS * FETCH_TRIGGER_DISTANCE_FACTOR) {
            isFetchingPlanets = true;
            System.out.println("SpacePanel: Player approaching edge of known area. Fetching more planets around: " + currentShipX + ", " + currentShipY);

            CompletableFuture.supplyAsync(() -> {
                try {
                    return Client.requestPlanetsArea(currentShipX, currentShipY, FETCH_RADIUS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    System.err.println("SpacePanel: Error fetching more planets in background: " + e.getMessage());
                    return null;
                }
            }).thenAccept(planetsData -> {
                SwingUtilities.invokeLater(() -> {
                    if (planetsData != null && planetsData.trim().startsWith("{") && planetsData.trim().endsWith("}")) {
                        spaceManager.parseAndStorePlanetsData(planetsData);
                        lastFetchGalaxyX = currentShipX;
                        lastFetchGalaxyY = currentShipY;
                        System.out.println("SpacePanel: More planets data received and parsed.");
                    } else {
                        System.err.println("SpacePanel: Failed to fetch more planets or no valid JSON data (async): " + planetsData);
                    }
                    isFetchingPlanets = false;
                });
            });
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        inputManager.keyTyped(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        inputManager.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        inputManager.keyReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (playerShip == null) return;

            double cameraTranslateX = getWidth() / 2.0 - playerShip.getX();
            double cameraTranslateY = getHeight() / 2.0 - playerShip.getY();

            Point2D.Double worldClickPoint = new Point2D.Double(
                e.getPoint().getX() - cameraTranslateX,
                e.getPoint().getY() - cameraTranslateY
            );

            if (spaceManager != null && spaceManager.getSpace() != null) {
                for (Planet planet : spaceManager.getLoadedPlanets()) {
                    if (planet.isNear(playerShip, PROXIMITY_THRESHOLD) && planet.getBounds().contains(worldClickPoint)) {
                        String jsonResponseString = null;
                        try {
                            jsonResponseString = Client.joinPlanet(planet.getPlanetId(), planet.getPlanetName());
                        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Error joining planet: " + ex.getMessage(), "Join Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        if (jsonResponseString != null) {
                            JoinGameResponse parsedResponse = null;
                            try {
                                parsedResponse = gson.fromJson(jsonResponseString, JoinGameResponse.class);
                            } catch (JsonSyntaxException ex) {
                                System.err.println("SpacePanel: Error parsing JoinGameResponse JSON: " + jsonResponseString + " - Error: " + ex.getMessage());
                                JOptionPane.showMessageDialog(this, "Failed to parse server response for " + planet.getPlanetName() + ".", "Join Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            if (parsedResponse != null && parsedResponse.success) {
                                JOptionPane.showMessageDialog(this, "Successfully joined " + planet.getPlanetName() + "! " + (parsedResponse.message != null ? parsedResponse.message : ""), "Join Success", JOptionPane.INFORMATION_MESSAGE);
                                parentScreen.dispose();
                                if (mainFrame != null) mainFrame.dispose(); 
                                new GameScreen(playerId, username, planet.getPlanetId(), planet.getPlanetName()).setVisible(true); 
                            } else {
                                String errorMessage = "Failed to join " + planet.getPlanetName() + ". ";
                                if (parsedResponse != null && parsedResponse.message != null) {
                                    errorMessage += parsedResponse.message;
                                } else if (jsonResponseString.length() > 100) {
                                    errorMessage += "Server returned an unexpected response.";
                                } else {
                                    errorMessage += jsonResponseString;
                                }
                                JOptionPane.showMessageDialog(this, errorMessage, "Join Failed", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                             JOptionPane.showMessageDialog(this, "Failed to join " + planet.getPlanetName() + ". No response from server.", "Join Failed", JOptionPane.ERROR_MESSAGE);
                        }
                        return; 
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    private void drawPlanetArrows(Graphics2D g2d, double camX, double camY) {
        if (playerShip == null || spaceManager == null || spaceManager.getSpace() == null) {
            return;
        }

        Point2D.Double shipScreenPos = new Point2D.Double(getWidth() / 2.0, getHeight() / 2.0);

        for (Planet planet : spaceManager.getLoadedPlanets()) {
            Point2D.Double planetWorldPos = new Point2D.Double(planet.getGalaxyX(), planet.getGalaxyY());
            Point2D.Double shipWorldPos = new Point2D.Double(playerShip.getX(), playerShip.getY());
            
            Point2D.Double planetScreenPos = new Point2D.Double(
                planetWorldPos.x - camX + getWidth() / 2.0,
                planetWorldPos.y - camY + getHeight() / 2.0
            );

            double distanceToPlanet = shipWorldPos.distance(planetWorldPos);

            if (distanceToPlanet < ARROW_VISIBILITY_RANGE) { 
                boolean isOnScreen = planetScreenPos.x >= 0 && planetScreenPos.x <= getWidth() &&
                                     planetScreenPos.y >= 0 && planetScreenPos.y <= getHeight();

                if (!isOnScreen) { 
                    drawArrowToOffscreenTarget(g2d, shipScreenPos, planetWorldPos, camX, camY);
                } else if (planet.isNear(playerShip, PROXIMITY_THRESHOLD * 2)) { 
                    g2d.setColor(new Color(ARROW_COLOR.getRed(), ARROW_COLOR.getGreen(), ARROW_COLOR.getBlue(), 100)); 
                    g2d.drawLine((int)shipScreenPos.x, (int)shipScreenPos.y, (int)planetScreenPos.x, (int)planetScreenPos.y);
                }
            }
        }
    }

    private void drawArrowToOffscreenTarget(Graphics2D g2d, Point2D.Double sourceScreenPos, Point2D.Double targetWorldPos, double playerWorldX, double playerWorldY) {
        double dxWorld = targetWorldPos.x - playerWorldX;
        double dyWorld = targetWorldPos.y - playerWorldY;
        
        double angle = Math.atan2(dyWorld, dxWorld);

        double screenCenterX = getWidth() / 2.0;
        double screenCenterY = getHeight() / 2.0;
        
        Point2D.Double intersection = getScreenEdgeIntersection(screenCenterX, screenCenterY, angle);

        if (intersection != null) {
            g2d.setColor(ARROW_COLOR);
            drawArrow(g2d, (int)Math.round(intersection.x), (int)Math.round(intersection.y), angle);
        }
    }

    private Point2D.Double getScreenEdgeIntersection(double centerX, double centerY, double angle) {
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);

        double k_min = Double.POSITIVE_INFINITY;
        Point2D.Double intersection = null;

        final double ANGLE_TOLERANCE = 1e-9;

        if (cosAngle < -ANGLE_TOLERANCE) {
            double k = -centerX / cosAngle;
            if (k >= -ANGLE_TOLERANCE) {
                double y = centerY + k * sinAngle;
                if (y >= -ANGLE_TOLERANCE && y <= getHeight() + ANGLE_TOLERANCE) {
                    y = Math.max(0, Math.min(getHeight(), y));
                    if (k < k_min) {
                        k_min = k;
                        intersection = new Point2D.Double(0, y);
                    }
                }
            }
        }

        if (cosAngle > ANGLE_TOLERANCE) {
            double k = (getWidth() - centerX) / cosAngle;
            if (k >= -ANGLE_TOLERANCE) {
                double y = centerY + k * sinAngle;
                if (y >= -ANGLE_TOLERANCE && y <= getHeight() + ANGLE_TOLERANCE) {
                    y = Math.max(0, Math.min(getHeight(), y));
                    if (k < k_min) {
                        k_min = k;
                        intersection = new Point2D.Double(getWidth(), y);
                    }
                }
            }
        }

        if (sinAngle < -ANGLE_TOLERANCE) {
            double k = -centerY / sinAngle;
            if (k >= -ANGLE_TOLERANCE) {
                double x = centerX + k * cosAngle;
                if (x >= -ANGLE_TOLERANCE && x <= getWidth() + ANGLE_TOLERANCE) {
                    x = Math.max(0, Math.min(getWidth(), x));
                    if (k < k_min) {
                        k_min = k;
                        intersection = new Point2D.Double(x, 0);
                    }
                }
            }
        }

        if (sinAngle > ANGLE_TOLERANCE) {
            double k = (getHeight() - centerY) / sinAngle;
            if (k >= -ANGLE_TOLERANCE) {
                double x = centerX + k * cosAngle;
                if (x >= -ANGLE_TOLERANCE && x <= getWidth() + ANGLE_TOLERANCE) {
                    x = Math.max(0, Math.min(getWidth(), x));
                    if (k < k_min) {
                        k_min = k;
                        intersection = new Point2D.Double(x, getHeight());
                    }
                }
            }
        }

        return intersection;
    }

    private void drawArrow(Graphics2D g2d, int x, int y, double angle) {
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(angle);
        
        int[] xPoints = {ARROW_SIZE, -ARROW_SIZE/2, -ARROW_SIZE/2};
        int[] yPoints = {0, -ARROW_SIZE/2, ARROW_SIZE/2};
        
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.setTransform(oldTransform);
    }
}
