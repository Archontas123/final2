package com.tavuc.ui.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.RadialGradientPaint;
import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.AlphaComposite;
import java.util.List;
import java.util.ArrayList;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;

/**
 * A custom UI component that renders a circular, stylized minimap. 
 */
public class MinimapComponent extends JPanel {
    /** The current world x and y coordinates of the player, displayed as text. */
    private int currentX, currentY;

    // Hollow Knight inspired colors - dark, atmospheric with subtle blues
    /** The color for the main border of the minimap. */
    private static final Color BORDER_COLOR = new Color(180, 190, 210, 80);
    /** The color for the soft glow effect around the border. */
    private static final Color BORDER_GLOW = new Color(120, 170, 220, 40);
    /** The primary accent color for glowing elements like the player icon and planets. */
    private static final Color ACCENT_COLOR = new Color(100, 180, 220);
    /** The color for the coordinate text display. */
    private static final Color TEXT_COLOR = new Color(200, 210, 230, 200);
    /** The base background color for the map area. */
    private static final Color MINIMAP_BG_COLOR = new Color(10, 15, 25, 180);
    /** The default color for planet icons on the minimap. */
    private static final Color MINIMAP_PLANET_COLOR = new Color(140, 160, 180);
    /** The color for enemy ship icons on the minimap. */
    private static final Color MINIMAP_ENEMY_COLOR = new Color(180, 60, 40);
    /** The color used for the "fog of war" effect at the edges of the map. */
    private static final Color FOG_COLOR = new Color(20, 30, 50, 120);
    
    /** The font used for displaying the player's coordinates. */
    private static final Font COORDS_FONT = new Font("Georgia", Font.ITALIC, 11);

    /** The diameter of the circular minimap area. */
    private static final int MINIMAP_SIZE = 160;
    /** The padding around the minimap within the component. */
    private static final int PADDING = 15;
    /** The thickness of the main border stroke. */
    private static final int BORDER_THICKNESS = 2;

    /** The scaling factor to convert world coordinates to minimap coordinates. */
    private static final double MINIMAP_SCALE = 0.05;
    /** A list of planets to be rendered on the minimap. */
    private List<Planet> planetsToDraw = new ArrayList<>();
    /** A list of other ships to be rendered on the minimap. */
    private List<Ship> shipsToDraw = new ArrayList<>();
    /** A reference to the player's ship, used to center the minimap view. */
    private Ship playerShipLocation;

    /**
     * Constructs a new MinimapComponent, setting its default size and properties.
     */
    public MinimapComponent() {
        setFocusable(false);
        setOpaque(false);
        
        int totalSize = MINIMAP_SIZE + 2 * PADDING;
        setPreferredSize(new Dimension(totalSize, totalSize));
    }

    /**
     * The main rendering method for the component. 
     * @param g The Graphics context to paint on.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawOrnateMapBorder(g2d);
        
        Ellipse2D.Float mapClip = new Ellipse2D.Float(PADDING, PADDING, MINIMAP_SIZE, MINIMAP_SIZE);
        g2d.setClip(mapClip);

        drawMapBackground(g2d);

        drawFogOfWar(g2d);

        double viewCenterX = (playerShipLocation != null) ? playerShipLocation.getX() : 0;
        double viewCenterY = (playerShipLocation != null) ? playerShipLocation.getY() : 0;
        
        int mapCenterX = PADDING + MINIMAP_SIZE / 2;
        int mapCenterY = PADDING + MINIMAP_SIZE / 2;

        for (Planet planet : planetsToDraw) {
            double planetMinimapX = (planet.getGalaxyX() - viewCenterX) * MINIMAP_SCALE + mapCenterX;
            double planetMinimapY = (planet.getGalaxyY() - viewCenterY) * MINIMAP_SCALE + mapCenterY;
            
            if (isInMapBounds(planetMinimapX, planetMinimapY, 10)) {
                drawPlanetWithGlow(g2d, planetMinimapX, planetMinimapY, planet);
            }
        }

        for (Ship ship : shipsToDraw) {
            if (ship == playerShipLocation) continue;
            double shipMinimapX = (ship.getX() - viewCenterX) * MINIMAP_SCALE + mapCenterX;
            double shipMinimapY = (ship.getY() - viewCenterY) * MINIMAP_SCALE + mapCenterY;
            
            if (isInMapBounds(shipMinimapX, shipMinimapY, 6)) {
                drawEnemyShip(g2d, shipMinimapX, shipMinimapY);
            }
        }
        
        g2d.setClip(null);
        
        drawPlayerIcon(g2d, mapCenterX, mapCenterY);

        drawCoordinates(g2d);
        
        g2d.dispose();
    }

    /**
     * Draws the ornate circular border of the minimap, including a multi-layered glow effect.
     * @param g2d The Graphics2D context to draw on.
     */
    private void drawOrnateMapBorder(Graphics2D g2d) {
        for (int i = 5; i > 0; i--) {
            g2d.setColor(new Color(BORDER_GLOW.getRed(), BORDER_GLOW.getGreen(), 
                                  BORDER_GLOW.getBlue(), BORDER_GLOW.getAlpha() / i));
            g2d.setStroke(new BasicStroke(i * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawOval(PADDING - i, PADDING - i, MINIMAP_SIZE + i * 2, MINIMAP_SIZE + i * 2);
        }
        
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(BORDER_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(PADDING, PADDING, MINIMAP_SIZE, MINIMAP_SIZE);
    }

    /**
     * Draws the background of the map area using a radial gradient to create a sense of depth.
     * @param g2d The Graphics2D context to draw on.
     */
    private void drawMapBackground(Graphics2D g2d) {
        Point2D center = new Point2D.Float(PADDING + MINIMAP_SIZE/2, PADDING + MINIMAP_SIZE/2);
        float radius = MINIMAP_SIZE/2;
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(15, 20, 35, 200),
            new Color(10, 15, 25, 220),
            new Color(5, 10, 20, 240)
        };
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        
        g2d.setPaint(gradient);
        g2d.fillOval(PADDING, PADDING, MINIMAP_SIZE, MINIMAP_SIZE);
    }

    /**
     * Draws a "fog of war" effect over the map, making the edges darker and less visible.
     * @param g2d The Graphics2D context to draw on.
     */
    private void drawFogOfWar(Graphics2D g2d) {
        Point2D center = new Point2D.Float(PADDING + MINIMAP_SIZE/2, PADDING + MINIMAP_SIZE/2);
        float radius = MINIMAP_SIZE/2;
        float[] dist = {0.0f, 0.6f, 1.0f};
        Color[] colors = {
            new Color(0, 0, 0, 0),
            new Color(FOG_COLOR.getRed(), FOG_COLOR.getGreen(), FOG_COLOR.getBlue(), 50),
            FOG_COLOR
        };
        RadialGradientPaint fogGradient = new RadialGradientPaint(center, radius, dist, colors);
        
        g2d.setPaint(fogGradient);
        g2d.fillOval(PADDING, PADDING, MINIMAP_SIZE, MINIMAP_SIZE);
    }

    /**
     * Draws a single planet icon on the minimap with a surrounding glow.
     * @param g2d The Graphics2D context to draw on.
     * @param x The x-coordinate on the minimap to draw the planet.
     * @param y The y-coordinate on the minimap to draw the planet.
     * @param planet The Planet object, used to determine size and color.
     */
    private void drawPlanetWithGlow(Graphics2D g2d, double x, double y, Planet planet) {
        int planetSize = Math.max(4, (int)(planet.getSize() * MINIMAP_SCALE * 0.2));
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(ACCENT_COLOR);
        g2d.fillOval((int)(x - planetSize), (int)(y - planetSize), 
                     planetSize * 2, planetSize * 2);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        Color planetColor = planet.getColor() != null ? planet.getColor() : MINIMAP_PLANET_COLOR;
        g2d.setColor(planetColor);
        g2d.fillOval((int)(x - planetSize/2), (int)(y - planetSize/2), 
                     planetSize, planetSize);
    }

    /**
     * Draws an icon representing an enemy or other non-player ship.
     * @param g2d The Graphics2D context to draw on.
     * @param x The x-coordinate on the minimap to draw the ship.
     * @param y The y-coordinate on the minimap to draw the ship.
     */
    private void drawEnemyShip(Graphics2D g2d, double x, double y) {
        Path2D.Double enemy = new Path2D.Double();
        enemy.moveTo(x, y - 3);
        enemy.lineTo(x + 3, y);
        enemy.lineTo(x, y + 3);
        enemy.lineTo(x - 3, y);
        enemy.closePath();
        
        g2d.setColor(new Color(MINIMAP_ENEMY_COLOR.getRed(), MINIMAP_ENEMY_COLOR.getGreen(), 
                              MINIMAP_ENEMY_COLOR.getBlue(), 180));
        g2d.fill(enemy);
    }

    /**
     * Draws the player's icon in the center of the minimap with a pulsing glow effect.
     * @param g2d The Graphics2D context to draw on.
     * @param centerX The center x-coordinate of the minimap.
     * @param centerY The center y-coordinate of the minimap.
     */
    private void drawPlayerIcon(Graphics2D g2d, int centerX, int centerY) {
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time * 0.003) * 0.2 + 0.8);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f * pulse));
        g2d.setColor(ACCENT_COLOR);
        g2d.fillOval(centerX - 8, centerY - 8, 16, 16);
        
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        Path2D.Double vessel = new Path2D.Double();
        vessel.moveTo(centerX, centerY - 6);
        vessel.curveTo(centerX + 4, centerY - 4, centerX + 4, centerY + 2, centerX, centerY + 4);
        vessel.curveTo(centerX - 4, centerY + 2, centerX - 4, centerY - 4, centerX, centerY - 6);
        vessel.closePath();
        
        g2d.setColor(ACCENT_COLOR);
        g2d.fill(vessel);
        g2d.setColor(new Color(240, 250, 255));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(vessel);
    }

    /**
     * Draws the player's current world coordinates as text below the minimap.
     * @param g2d The Graphics2D context to draw on.
     */
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setFont(COORDS_FONT);
        g2d.setColor(TEXT_COLOR);
        String coordsText = currentX + ", " + currentY;
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(coordsText);
        int x = PADDING + (MINIMAP_SIZE - textWidth) / 2;
        int y = PADDING + MINIMAP_SIZE + 12;
        
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(coordsText, x + 1, y + 1);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(coordsText, x, y);
    }

    /**
     * Checks if a given point on the minimap is within the visible circular area.
     * @param x The x-coordinate of the point on the minimap.
     * @param y The y-coordinate of the point on the minimap.
     * @param margin A buffer zone to prevent icons from being clipped at the very edge.
     * @return {@code true} if the point is within the map bounds, {@code false} otherwise.
     */
    private boolean isInMapBounds(double x, double y, int margin) {
        double dx = x - (PADDING + MINIMAP_SIZE/2);
        double dy = y - (PADDING + MINIMAP_SIZE/2);
        double distance = Math.sqrt(dx*dx + dy*dy);
        return distance <= (MINIMAP_SIZE/2 - margin);
    }

    /**
     * Updates the coordinates displayed below the minimap.
     * @param x The new x-coordinate to display.
     * @param y The new y-coordinate to display.
     */
    public void updateCoordinates(int x, int y) {
        this.currentX = x;
        this.currentY = y;
        repaint();
    }

    /**
     * Updates the minimap with the latest game state data to be rendered.
     * @param planets A list of all planets to potentially draw.
     * @param ships A list of all other ships to potentially draw.
     * @param playerShip A reference to the player's ship for centering the view.
     */
    public void updateMinimapData(List<Planet> planets, List<Ship> ships, Ship playerShip) {
        this.planetsToDraw = planets != null ? new ArrayList<>(planets) : new ArrayList<>();
        this.shipsToDraw = ships != null ? new ArrayList<>(ships) : new ArrayList<>();
        this.playerShipLocation = playerShip;
        repaint();
    }
}