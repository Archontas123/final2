package com.tavuc.ui.components;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.Polygon;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.ArrayList;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;

public class MinimapComponent extends JPanel {
    private int currentX, currentY;

    // Styled colors based on ui_concept.html
    private static final Color BORDER_COLOR = new Color(70, 75, 85);
    private static final Color ACCENT_COLOR = new Color(120, 170, 190);
    private static final Color TEXT_MUTED_COLOR = new Color(160, 160, 170);
    private static final Color MINIMAP_AREA_BG_COLOR = new Color(20, 25, 40, 200); // Alpha for transparency
    private static final Color MINIMAP_PLANET_COLOR = new Color(80, 90, 110);
    private static final Color MINIMAP_ENEMY_COLOR = new Color(110, 20, 20);
    
    // Using logical fonts as placeholders for "Cormorant Garamond"
    // Proper font loading from file would be needed for custom fonts in a real application.
    private static final Font UI_FONT_BOLD = new Font("Serif", Font.BOLD, 16);
    private static final Font COORDS_FONT = new Font("Serif", Font.PLAIN, 12);

    private static final int MINIMAP_INTERNAL_SIZE = 150;
    private static final int PADDING = 10;
    private static final int TITLE_AREA_HEIGHT = 30; // Includes title and separator line
    private static final int COORDS_LABEL_HEIGHT = 20;
    private static final int BORDER_THICKNESS = 2;
    private static final int ROUND_CORNER_ARC = 10;

    private static final double MINIMAP_SCALE = 0.05;
    private List<Planet> planetsToDraw = new ArrayList<>();
    private List<Ship> shipsToDraw = new ArrayList<>();
    private Ship playerShipLocation;

    public MinimapComponent() {
        setFocusable(false);
        setLayout(new BorderLayout()); // Simplified layout
        setOpaque(false); // We will draw our own background

        int totalWidth = MINIMAP_INTERNAL_SIZE + 2 * PADDING;
        // Adjusted height: just the map area and its padding
        int totalHeight = MINIMAP_INTERNAL_SIZE + 2 * PADDING; 
        setPreferredSize(new Dimension(totalWidth, totalHeight));
        // mapArea is no longer needed as a separate component.
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Map Area Background
        g2d.setColor(MINIMAP_AREA_BG_COLOR);
        // Draw the map background slightly inset if PADDING is meant for outside the map area itself
        // Or, if PADDING is internal, draw from 0,0 up to MINIMAP_INTERNAL_SIZE
        // Assuming PADDING is for the component, and MINIMAP_INTERNAL_SIZE is the actual map drawing space
        g2d.fillRect(PADDING, PADDING, MINIMAP_INTERNAL_SIZE, MINIMAP_INTERNAL_SIZE);

        // Map Area Border
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(PADDING, PADDING, MINIMAP_INTERNAL_SIZE -1 , MINIMAP_INTERNAL_SIZE -1); // -1 for sharp border

        // Viewport center for minimap rendering (player's position)
        double viewCenterX = (playerShipLocation != null) ? playerShipLocation.getX() : 0;
        double viewCenterY = (playerShipLocation != null) ? playerShipLocation.getY() : 0;

        // Map drawing origin (top-left of the actual map drawing area)
        int mapDrawingOriginX = PADDING;
        int mapDrawingOriginY = PADDING;
        // Center of the map drawing area for calculations
        double mapCenterX = mapDrawingOriginX + MINIMAP_INTERNAL_SIZE / 2.0;
        double mapCenterY = mapDrawingOriginY + MINIMAP_INTERNAL_SIZE / 2.0;


        for (Planet planet : planetsToDraw) {
            double planetMinimapX = (planet.getGalaxyX() - viewCenterX) * MINIMAP_SCALE + mapCenterX;
            double planetMinimapY = (planet.getGalaxyY() - viewCenterY) * MINIMAP_SCALE + mapCenterY;
            int planetMinimapSize = Math.max(3, (int)(planet.getSize() * MINIMAP_SCALE * 0.15));

            // Check if within the map drawing area bounds (relative to mapDrawingOriginX/Y)
            if (planetMinimapX >= mapDrawingOriginX - planetMinimapSize && planetMinimapX <= mapDrawingOriginX + MINIMAP_INTERNAL_SIZE + planetMinimapSize && 
                planetMinimapY >= mapDrawingOriginY - planetMinimapSize && planetMinimapY <= mapDrawingOriginY + MINIMAP_INTERNAL_SIZE + planetMinimapSize) {
                g2d.setColor(planet.getColor() != null ? planet.getColor() : MINIMAP_PLANET_COLOR);
                g2d.fillOval((int)(planetMinimapX - planetMinimapSize / 2.0), 
                               (int)(planetMinimapY - planetMinimapSize / 2.0), 
                               planetMinimapSize, planetMinimapSize);
            }
        }

        for (Ship ship : shipsToDraw) {
            if (ship == playerShipLocation) continue;
            double shipMinimapX = (ship.getX() - viewCenterX) * MINIMAP_SCALE + mapCenterX;
            double shipMinimapY = (ship.getY() - viewCenterY) * MINIMAP_SCALE + mapCenterY;
            int shipMinimapSize = 4;

            if (shipMinimapX >= mapDrawingOriginX - shipMinimapSize && shipMinimapX <= mapDrawingOriginX + MINIMAP_INTERNAL_SIZE + shipMinimapSize && 
                shipMinimapY >= mapDrawingOriginY - shipMinimapSize && shipMinimapY <= mapDrawingOriginY + MINIMAP_INTERNAL_SIZE + shipMinimapSize) {
                g2d.setColor(MINIMAP_ENEMY_COLOR);
                g2d.fillRect((int)(shipMinimapX - shipMinimapSize / 2.0), 
                               (int)(shipMinimapY - shipMinimapSize / 2.0), 
                               shipMinimapSize, shipMinimapSize);
            }
        }
        
        // Player Icon (drawn in the center of the map drawing area)
        g2d.setColor(ACCENT_COLOR);
        int playerIconSize = 8;
        Polygon playerDiamond = new Polygon();
        playerDiamond.addPoint((int)mapCenterX, (int)(mapCenterY - playerIconSize / 2.0));
        playerDiamond.addPoint((int)(mapCenterX + playerIconSize / 2.0), (int)mapCenterY);
        playerDiamond.addPoint((int)mapCenterX, (int)(mapCenterY + playerIconSize / 2.0));
        playerDiamond.addPoint((int)(mapCenterX - playerIconSize / 2.0), (int)mapCenterY);
        g2d.fillPolygon(playerDiamond);

        // Draw coordinates at the bottom of the map area
        g2d.setFont(COORDS_FONT);
        g2d.setColor(TEXT_MUTED_COLOR);
        String coordsText = "X: " + currentX + ", Y: " + currentY;
        java.awt.FontMetrics fmCoords = g2d.getFontMetrics();
        int coordsTextWidth = fmCoords.stringWidth(coordsText);
        // Position at bottom-center of the map drawing area, slightly above the bottom border
        g2d.drawString(coordsText, 
                       mapDrawingOriginX + (MINIMAP_INTERNAL_SIZE - coordsTextWidth) / 2, 
                       mapDrawingOriginY + MINIMAP_INTERNAL_SIZE - fmCoords.getDescent() - 2);
        
        g2d.dispose();
    }

    public void updateCoordinates(int x, int y) {
        this.currentX = x;
        this.currentY = y;
        repaint(); // Repaint to update coordinates text if drawn directly
    }

    public void updateMinimapData(List<Planet> planets, List<Ship> ships, Ship playerShip) {
        this.planetsToDraw = planets != null ? new ArrayList<>(planets) : new ArrayList<>();
        this.shipsToDraw = ships != null ? new ArrayList<>(ships) : new ArrayList<>();
        this.playerShipLocation = playerShip;
        repaint(); // Repaint this component directly
    }
}
