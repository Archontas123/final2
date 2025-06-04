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

public class MinimapComponent extends JPanel {
    private int currentX, currentY;

    // Hollow Knight inspired colors - dark, atmospheric with subtle blues
    private static final Color BORDER_COLOR = new Color(180, 190, 210, 80);
    private static final Color BORDER_GLOW = new Color(120, 170, 220, 40);
    private static final Color ACCENT_COLOR = new Color(100, 180, 220);
    private static final Color TEXT_COLOR = new Color(200, 210, 230, 200);
    private static final Color MINIMAP_BG_COLOR = new Color(10, 15, 25, 180);
    private static final Color MINIMAP_PLANET_COLOR = new Color(140, 160, 180);
    private static final Color MINIMAP_ENEMY_COLOR = new Color(180, 60, 40);
    private static final Color FOG_COLOR = new Color(20, 30, 50, 120);
    
    // Elegant serif font for that hand-drawn feel
    private static final Font COORDS_FONT = new Font("Georgia", Font.ITALIC, 11);

    private static final int MINIMAP_SIZE = 160;
    private static final int PADDING = 15;
    private static final int BORDER_THICKNESS = 2;

    private static final double MINIMAP_SCALE = 0.05;
    private List<Planet> planetsToDraw = new ArrayList<>();
    private List<Ship> shipsToDraw = new ArrayList<>();
    private Ship playerShipLocation;

    public MinimapComponent() {
        setFocusable(false);
        setOpaque(false);
        
        int totalSize = MINIMAP_SIZE + 2 * PADDING;
        setPreferredSize(new Dimension(totalSize, totalSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw ornate border with glow effect
        drawOrnateMapBorder(g2d);
        
        // Create circular clip for map content
        Ellipse2D.Float mapClip = new Ellipse2D.Float(PADDING, PADDING, MINIMAP_SIZE, MINIMAP_SIZE);
        g2d.setClip(mapClip);

        // Draw map background with subtle gradient
        drawMapBackground(g2d);

        // Draw fog of war effect at edges
        drawFogOfWar(g2d);

        // Map content
        double viewCenterX = (playerShipLocation != null) ? playerShipLocation.getX() : 0;
        double viewCenterY = (playerShipLocation != null) ? playerShipLocation.getY() : 0;
        
        int mapCenterX = PADDING + MINIMAP_SIZE / 2;
        int mapCenterY = PADDING + MINIMAP_SIZE / 2;

        // Draw planets with glow effects
        for (Planet planet : planetsToDraw) {
            double planetMinimapX = (planet.getGalaxyX() - viewCenterX) * MINIMAP_SCALE + mapCenterX;
            double planetMinimapY = (planet.getGalaxyY() - viewCenterY) * MINIMAP_SCALE + mapCenterY;
            
            if (isInMapBounds(planetMinimapX, planetMinimapY, 10)) {
                drawPlanetWithGlow(g2d, planetMinimapX, planetMinimapY, planet);
            }
        }

        // Draw enemy ships
        for (Ship ship : shipsToDraw) {
            if (ship == playerShipLocation) continue;
            double shipMinimapX = (ship.getX() - viewCenterX) * MINIMAP_SCALE + mapCenterX;
            double shipMinimapY = (ship.getY() - viewCenterY) * MINIMAP_SCALE + mapCenterY;
            
            if (isInMapBounds(shipMinimapX, shipMinimapY, 6)) {
                drawEnemyShip(g2d, shipMinimapX, shipMinimapY);
            }
        }
        
        // Reset clip for player icon
        g2d.setClip(null);
        
        // Draw player icon with special effect
        drawPlayerIcon(g2d, mapCenterX, mapCenterY);

        // Draw coordinates
        drawCoordinates(g2d);
        
        g2d.dispose();
    }

    private void drawOrnateMapBorder(Graphics2D g2d) {
        // Outer glow
        for (int i = 5; i > 0; i--) {
            g2d.setColor(new Color(BORDER_GLOW.getRed(), BORDER_GLOW.getGreen(), 
                                  BORDER_GLOW.getBlue(), BORDER_GLOW.getAlpha() / i));
            g2d.setStroke(new BasicStroke(i * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawOval(PADDING - i, PADDING - i, MINIMAP_SIZE + i * 2, MINIMAP_SIZE + i * 2);
        }
        
        // Main border
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(BORDER_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawOval(PADDING, PADDING, MINIMAP_SIZE, MINIMAP_SIZE);
    }

    private void drawMapBackground(Graphics2D g2d) {
        // Create radial gradient for depth
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

    private void drawFogOfWar(Graphics2D g2d) {
        // Create fog effect at edges
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

    private void drawPlanetWithGlow(Graphics2D g2d, double x, double y, Planet planet) {
        int planetSize = Math.max(4, (int)(planet.getSize() * MINIMAP_SCALE * 0.2));
        
        // Planet glow
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(ACCENT_COLOR);
        g2d.fillOval((int)(x - planetSize), (int)(y - planetSize), 
                     planetSize * 2, planetSize * 2);
        
        // Planet body
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        Color planetColor = planet.getColor() != null ? planet.getColor() : MINIMAP_PLANET_COLOR;
        g2d.setColor(planetColor);
        g2d.fillOval((int)(x - planetSize/2), (int)(y - planetSize/2), 
                     planetSize, planetSize);
    }

    private void drawEnemyShip(Graphics2D g2d, double x, double y) {
        // Enemy ship as a small diamond shape
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

    private void drawPlayerIcon(Graphics2D g2d, int centerX, int centerY) {
        // Player icon with pulsing effect
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time * 0.003) * 0.2 + 0.8);
        
        // Outer glow
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f * pulse));
        g2d.setColor(ACCENT_COLOR);
        g2d.fillOval(centerX - 8, centerY - 8, 16, 16);
        
        // Player vessel shape (inspired by Hollow Knight's vessel)
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

    private void drawCoordinates(Graphics2D g2d) {
        g2d.setFont(COORDS_FONT);
        g2d.setColor(TEXT_COLOR);
        String coordsText = currentX + ", " + currentY;
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(coordsText);
        int x = PADDING + (MINIMAP_SIZE - textWidth) / 2;
        int y = PADDING + MINIMAP_SIZE + 12;
        
        // Text shadow for readability
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(coordsText, x + 1, y + 1);
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(coordsText, x, y);
    }

    private boolean isInMapBounds(double x, double y, int margin) {
        double dx = x - (PADDING + MINIMAP_SIZE/2);
        double dy = y - (PADDING + MINIMAP_SIZE/2);
        double distance = Math.sqrt(dx*dx + dy*dy);
        return distance <= (MINIMAP_SIZE/2 - margin);
    }

    public void updateCoordinates(int x, int y) {
        this.currentX = x;
        this.currentY = y;
        repaint();
    }

    public void updateMinimapData(List<Planet> planets, List<Ship> ships, Ship playerShip) {
        this.planetsToDraw = planets != null ? new ArrayList<>(planets) : new ArrayList<>();
        this.shipsToDraw = ships != null ? new ArrayList<>(ships) : new ArrayList<>();
        this.playerShipLocation = playerShip;
        repaint();
    }
}