package com.tavuc.ui.panels;

import javax.swing.*;

import com.tavuc.Client;
import com.tavuc.managers.SpaceManager;
import com.tavuc.managers.InputManager;
import com.tavuc.models.space.Moon;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;
import com.tavuc.models.space.Projectile;
import com.tavuc.networking.models.ProjectileSpawnedBroadcast;
import com.tavuc.networking.models.ProjectileUpdateBroadcast;
import com.tavuc.networking.models.ProjectileRemovedBroadcast;
import com.tavuc.networking.models.ShipDamagedBroadcast;
import com.tavuc.networking.models.ShipDestroyedBroadcast;
import com.tavuc.ecs.components.HealthComponent;
import com.tavuc.ecs.systems.ShipCombatSystem;
import com.tavuc.ui.panels.GameOverPanel;
import com.tavuc.ui.screens.GScreen;
import com.tavuc.ui.screens.SpaceScreen;
import com.tavuc.ui.screens.GameScreen;
import com.tavuc.networking.models.JoinGameResponse;
import com.google.gson.Gson;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.tavuc.ui.components.DamagePopup;

public class SpacePanel extends GPanel implements KeyListener, MouseListener, ActionListener, ISpacePanel {

    // Star field data
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

    // Player and world data
    private Ship playerShip;
    private SpaceManager spaceManager;
    private InputManager inputManager;
    private Timer gameLoopTimer;
    private SpaceScreen parentScreen;
    private int playerId;
    private String username;
    private JFrame mainFrame;
    
    // World dimensions
    private static final int WORLD_WIDTH = 800 * 5; 
    private static final int WORLD_HEIGHT = 600 * 5;
    
    // Environment
    private List<StarData> starField;
    private static final int NUM_STARS = 200;
    
    // Other players
    private Map<Integer, Ship> otherPlayerShips = new ConcurrentHashMap<>();
    private boolean renderOtherShips = true;
    
    // Combat system
    private ShipCombatSystem combatSystem;
    
    // Game state
    private boolean gameOver = false;

    // Planet fetching logic
    private double lastFetchGalaxyX = 0;
    private double lastFetchGalaxyY = 0;
    private volatile boolean isFetchingPlanets = false;
    private static final double FETCH_RADIUS = 1500;
    private static final double FETCH_TRIGGER_DISTANCE_FACTOR = 0.75;

    // Visual indicators
    private static final double ARROW_VISIBILITY_RANGE = 2000.0;
    private static final int ARROW_SIZE = 15;
    private static final Color ARROW_COLOR = Color.GREEN;

    // Floating damage numbers
    private final List<DamagePopup> damagePopups = new ArrayList<>();

    public SpacePanel(SpaceScreen parentScreen, JFrame mainFrame, int playerId, String username, SpaceManager spaceManager) {
        super();
        this.parentScreen = parentScreen; 
        this.mainFrame = mainFrame;
        this.playerId = playerId;
        this.username = username;
        this.spaceManager = spaceManager;

        setOpaque(true); 
        setBackground(Color.BLACK);

        // Initialize player ship slightly offset from the center so multiple
        // players don't stack directly on top of each other when respawning
        double SPAWN_RADIUS = 200.0;
        java.util.Random rand = new java.util.Random();
        double angle = rand.nextDouble() * Math.PI * 2.0;
        double distance = rand.nextDouble() * SPAWN_RADIUS;
        double spawnX = WORLD_WIDTH / 2.0 + Math.cos(angle) * distance;
        double spawnY = WORLD_HEIGHT / 2.0 + Math.sin(angle) * distance;

        this.playerShip = new Ship(spawnX, spawnY);
        this.spaceManager.addShip(this.playerShip);
        
        // Initialize combat system
        this.combatSystem = new ShipCombatSystem(playerShip);
        
        this.inputManager = InputManager.getInstance();
        this.inputManager.setShipTarget(this.playerShip);
        this.inputManager.setControlTarget(InputManager.ControlTargetType.SHIP);

        addKeyListener(this);
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
        
        Client.currentSpacePanel = this;
        
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
            } catch (Exception e) {
                System.err.println("SpacePanel: Error fetching initial planets in background: " + e.getMessage());
                e.printStackTrace();
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
                repaint();
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

        // Draw planets
        if (spaceManager != null && spaceManager.getSpace() != null) {
            for (Planet planet : spaceManager.getLoadedPlanets()) {
                planet.draw(g2d); 
            }
        }

        // Draw player ship if not destroyed
        if (playerShip != null && !playerShip.isDestroyed() && Client.currentSpacePanel == this) {
            playerShip.draw(g2d);
        }

        // Draw other players' ships
        if (renderOtherShips) {
            for (Ship otherShip : otherPlayerShips.values()) {
                if (!otherShip.isDestroyed()) {
                    otherShip.draw(g2d);
                }
            }
        }
        
        // Draw combat elements using the combat system's render data
        if (combatSystem != null) {
            // Draw projectiles
            List<ShipCombatSystem.ProjectileRenderData> projectiles = combatSystem.getProjectilesToRender();
            for (ShipCombatSystem.ProjectileRenderData projectile : projectiles) {
                g2d.setColor(projectile.color);
                g2d.fillOval(
                    (int)(projectile.x - projectile.size/2),
                    (int)(projectile.y - projectile.size/2),
                    projectile.size,
                    projectile.size
                );
            }

            // Draw explosions
            List<ShipCombatSystem.ExplosionRenderData> explosions = combatSystem.getExplosionsToRender();
            for (ShipCombatSystem.ExplosionRenderData explosion : explosions) {
                drawExplosion(g2d, explosion);
            }
        }

        // Draw floating damage numbers
        Iterator<DamagePopup> iter = damagePopups.iterator();
        while (iter.hasNext()) {
            DamagePopup dp = iter.next();
            if (dp.update()) {
                iter.remove();
            } else {
                dp.draw(g2d, 0, 0);
            }
        }

        g2d.setTransform(originalTransform);

        drawPlanetArrows(g2d, camX, camY);
        
        // If game is over, no additional HUD needed here
        if (gameOver) {
            return;
        }
    }

    /**
     * Draws an explosion effect based on the provided explosion render data.
     */
    private void drawExplosion(Graphics2D g2d, ShipCombatSystem.ExplosionRenderData explosion) {
        // Save the current transform
        AffineTransform oldTransform = g2d.getTransform();

        // Translate to explosion center
        g2d.translate(explosion.x, explosion.y);
        g2d.scale(explosion.scale, explosion.scale);

        // Create colors with the current alpha value
        Color[] explosionColors = {
            new Color(1.0f, 1.0f, 0.2f, explosion.alpha * 0.8f),
            new Color(1.0f, 0.5f, 0.0f, explosion.alpha * 0.6f),
            new Color(1.0f, 0.2f, 0.0f, explosion.alpha * 0.4f)
        };

        int[] sizes = {30, 50, 70};

        for (int i = 0; i < explosionColors.length; i++) {
            g2d.setColor(explosionColors[i]);
            g2d.fillOval(-sizes[i]/2, -sizes[i]/2, sizes[i], sizes[i]);
        }

        // Restore original transform
        g2d.setTransform(oldTransform);
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

    /** Adds a floating damage popup at the given world position. */
    private void addDamagePopup(double x, double y, double damage) {
        String txt = String.format("%.1f", damage);
        Color c = (damage == 0) ? new Color(80, 160, 220) : new Color(220, 60, 40);
        damagePopups.add(new DamagePopup(x, y, txt, c));
    }

    @Override
    public void actionPerformed(ActionEvent e) { 
        if (e.getSource() == gameLoopTimer) {
            gameUpdate();
        }
    }

    private void gameUpdate() {
        if (gameOver) {
            return;
        }
        
        // Check if player ship is destroyed
        if (playerShip.isDestroyed()) {
            handlePlayerDestroyed();
            return;
        }

        // Calculate delta time (assuming 60 FPS for now)
        double deltaTime = 1.0 / 60.0;
        
        spaceManager.tick(); 
        sendPlayerShipData();
        
        // Update combat system
        List<Ship> otherShips = new ArrayList<>(otherPlayerShips.values());
        combatSystem.update(deltaTime, otherShips);

        if (parentScreen != null && playerShip != null) {
            parentScreen.updatePlayerCoordinatesOnUI(playerShip.getX(), playerShip.getY());
        }

        // Update UI Components with health and shield data
        if (parentScreen != null && playerShip != null && spaceManager != null && inputManager != null) {
            List<Ship> allShipsForUI = new ArrayList<>(otherPlayerShips.values());
            allShipsForUI.add(playerShip);
            List<Planet> loadedPlanetsListForUI = new ArrayList<>(spaceManager.getLoadedPlanets());
            
            // Get health and shield percentages
            int healthPercent = (int)playerShip.getHealthPercentage();
            int shieldPercent = (int)playerShip.getShieldPercentage();
            
            parentScreen.updateUILayerData(
                (int)playerShip.getX(), (int)playerShip.getY(),
                loadedPlanetsListForUI,
                allShipsForUI,
                playerShip,
                inputManager.isKeyPressed(KeyEvent.VK_W),
                inputManager.isKeyPressed(KeyEvent.VK_A),
                inputManager.isKeyPressed(KeyEvent.VK_S),
                inputManager.isKeyPressed(KeyEvent.VK_D),
                healthPercent,
                shieldPercent,
                null // dialog text - can be added later
            );
        }

        // Check for boarding
        if (spaceManager != null && spaceManager.getSpace() != null) {
            for (Planet planet : spaceManager.getLoadedPlanets()) {
                planet.isNear(playerShip, 40); // PROXIMITY_THRESHOLD
            }
        }

        checkCollisions();
        fetchMorePlanetsIfNeeded(); 

        repaint();
    }
    
    /**
     * Handles player ship destruction
     */
    private void handlePlayerDestroyed() {
        if (!gameOver) {
            gameOver = true;
            combatSystem.handlePlayerDestroyed();

            // Show game over panel after a short delay
            Timer gameOverTimer = new Timer(3000, e -> {
                SwingUtilities.invokeLater(() -> {
                    if (parentScreen != null) {
                        GameOverPanel panel = new GameOverPanel(parentScreen, playerId, username);
                        parentScreen.showOverlay(panel);
                    }
                });
            });
            gameOverTimer.setRepeats(false);
            gameOverTimer.start();
        }
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
    
    /**
     * Get all other player ships for collision detection.
     */
    public Collection<Ship> getOtherPlayerShips() {
        return otherPlayerShips.values();
    }

    private void checkCollisions() {
        if (playerShip == null) return;
        // Collision is handled by the combat system
    }

    /**
     * Handles a projectile spawned broadcast from the server.
     * @param event The ProjectileSpawnedBroadcast from the server
     */
    public void handleProjectileSpawned(ProjectileSpawnedBroadcast event) {
        double velocityX = event.velocityX;
        double velocityY = event.velocityY;

        Projectile projectile = new Projectile(
            event.projectileId,
            event.x,
            event.y,
            velocityX,
            velocityY,
            event.damage,
            event.firedBy
        );

        if (combatSystem != null) {
            combatSystem.addRemoteProjectile(projectile);
        }
    }

    public void handleProjectileUpdate(ProjectileUpdateBroadcast event) {
        if (combatSystem != null) {
            combatSystem.updateProjectile(event.projectileId, event.x, event.y, event.velocityX, event.velocityY);
        }
    }

    public void handleProjectileRemoved(ProjectileRemovedBroadcast event) {
        if (combatSystem != null) {
            combatSystem.removeProjectile(event.projectileId);
        }
    }

    public void handleShipDamaged(ShipDamagedBroadcast event) {
        try {
            int id = Integer.parseInt(event.playerId);
            if (id == this.playerId) {
                if (playerShip != null) {
                    playerShip.updateHealthFromServer(event.currentHealth, event.maxHealth);
                    playerShip.triggerHitEffect();
                    addDamagePopup(playerShip.getX(), playerShip.getY(), event.damageAmount);
                }
            } else {
                Ship ship = otherPlayerShips.get(id);
                if (ship != null) {
                    ship.updateHealthFromServer(event.currentHealth, event.maxHealth);
                    ship.triggerHitEffect();
                    if (event.currentHealth <= 0) {
                        ship.setDestroyed(true);
                    }
                    addDamagePopup(ship.getX(), ship.getY(), event.damageAmount);
                }
            }
        } catch (NumberFormatException ex) {
            System.err.println("SpacePanel: Invalid playerId in ShipDamagedBroadcast: " + event.playerId);
        }
    }

    /**
     * Handles a ship destroyed broadcast from the server.
     */
    public void handleShipDestroyed(ShipDestroyedBroadcast event) {
        try {
            int id = Integer.parseInt(event.playerId);
            if (combatSystem != null) {
                combatSystem.createExplosion(event.x, event.y);
            }

            if (id == this.playerId) {
                if (playerShip != null) {
                    playerShip.setDestroyed(true);
                }
                handlePlayerDestroyed();
            } else {
                Ship ship = otherPlayerShips.get(id);
                if (ship != null) {
                    ship.setDestroyed(true);
                }
            }
        } catch (NumberFormatException ex) {
            System.err.println("SpacePanel: Invalid playerId in ShipDestroyedBroadcast: " + event.playerId);
        }
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
                } catch (Exception e) {
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
        // No action needed
    }

    @Override
    public void keyPressed(KeyEvent e) {
        inputManager.keyPressed(e);
        
        // Handle fire key (space bar)
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameOver && combatSystem != null) {
                combatSystem.fireProjectile();
            }
        }
        
        // Handle ESC key for game over
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && gameOver) {
            // Return to main menu or similar action
            parentScreen.dispose();
            Client.returnToShip(); // Or other appropriate action
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        inputManager.keyReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (gameOver) return;
        
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
                    if (planet.isNear(playerShip, 40) && planet.getBounds().contains(worldClickPoint)) {
                        // Attempt to join planet
                        try {
                            String jsonResponseString = Client.joinPlanet(planet.getPlanetId(), planet.getPlanetName());
                            JoinGameResponse resp = new Gson().fromJson(jsonResponseString, JoinGameResponse.class);
                            if (resp != null && resp.success) {
                                SwingUtilities.invokeLater(() -> {
                                    if (parentScreen != null) {
                                        parentScreen.dispose();
                                    }
                                    GameScreen gameScreen = new GameScreen(Client.getInstance().getPlayerId(),
                                            Client.getInstance().getUsername(), Integer.parseInt(resp.gameId), resp.planetName);
                                    gameScreen.setVisible(true);
                                });
                            } else {
                                String msg = (resp != null) ? resp.message : "Unknown error";
                                JOptionPane.showMessageDialog(this, "Failed to join planet: " + msg, "Join Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error joining planet: " + ex.getMessage(), "Join Error", JOptionPane.ERROR_MESSAGE);
                        }
                        return;
                    }
                }
            }
            
            // If no planet clicked, fire a projectile
            if (combatSystem != null) {
                combatSystem.fireProjectile();
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
                } else if (planet.isNear(playerShip, 80)) { 
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
    
    /**
     * Returns the player ship for external systems.
     */
    public Ship getPlayerShip() {
        return playerShip;
    }
}