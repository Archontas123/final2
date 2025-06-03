package com.tavuc.models.space;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.tavuc.models.planets.PlanetType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List; 
import java.util.ArrayList; 


public class Planet {

    private int planetId;
    private String planetName;
    private PlanetType planetType;
    private double x, y; 
    private int radius; 
    private boolean isBoardingIndicatorActive;
    private Color representativeColor; 
    private Color activeHueShiftColor;
    private int galaxyX; 
    private int galaxyY; 
    private transient BufferedImage basePlanetIcon;
    private transient BufferedImage displayedPlanetIcon;
    private static final int ICON_SIZE = 128;

    /**
     * Constructor for client-side Planet
     * @param planetId Unique ID of the planet
     * @param planetName Name of the planet
     * @param planetType Type of the planet
     * @param radius Visual radius in the picker (will be adjusted for icon size)
     * @param representativeColor Primary color for display (fallback)
     * @param galaxyX Absolute X coordinate in the galaxy
     * @param galaxyY Absolute Y coordinate in the galaxy
     * @param hueShiftColor The color to use for hue shifting the icon
     */
    public Planet(int planetId, String planetName, PlanetType planetType, int radius, Color representativeColor, int galaxyX, int galaxyY, Color hueShiftColor) {
        this.planetId = planetId;
        this.planetName = planetName;
        this.planetType = planetType;
        this.radius = ICON_SIZE / 2; 
        this.representativeColor = representativeColor;
        this.activeHueShiftColor = hueShiftColor;
        this.galaxyX = galaxyX;
        this.galaxyY = galaxyY;
        this.x = galaxyX; 
        this.y = galaxyY; 
        this.isBoardingIndicatorActive = false;
    }
    
    private void loadBaseIcon() {
        String resourcePath = "assets/planets/exterior/planet_" +  planetType.name().toLowerCase() + ".png";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.err.println("Planet icon resource not found: " + resourcePath);
                basePlanetIcon = null;
                return;
            }
            basePlanetIcon = ImageIO.read(inputStream);
        } catch (IOException e) {
            System.err.println("Failed to load planet icon for type " + planetType + " from resource " + resourcePath + ": " + e.getMessage());
            basePlanetIcon = null; 
        }
        
    }

    private void prepareDisplayedIcon() {
        loadBaseIcon(); 
        displayedPlanetIcon = applyHueShift(basePlanetIcon, activeHueShiftColor);
    }
        

    private BufferedImage applyHueShift(BufferedImage sourceImage, Color hueTargetColor) {

        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        BufferedImage shiftedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float[] targetHSB = Color.RGBtoHSB(hueTargetColor.getRed(), hueTargetColor.getGreen(), hueTargetColor.getBlue(), null);
        float targetHue = targetHSB[0];

        for (int yPx = 0; yPx < height; yPx++) {
            for (int xPx = 0; xPx < width; xPx++) {
                int argb = sourceImage.getRGB(xPx, yPx);
                int alpha = (argb >> 24) & 0xFF;
                
                if (alpha == 0) { 
                    shiftedImage.setRGB(xPx, yPx, argb);
                    continue;
                }

                Color pixelColor = new Color(argb, true); 
                float[] pixelHSB = Color.RGBtoHSB(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue(), null);
                
                Color newPixelColor = Color.getHSBColor(targetHue, pixelHSB[1], pixelHSB[2]);
                
                int newRGB = (alpha << 24) | (newPixelColor.getRed() << 16) | (newPixelColor.getGreen() << 8) | newPixelColor.getBlue();
                shiftedImage.setRGB(xPx, yPx, newRGB);
            }
        }
        return shiftedImage;
    }

    /**
     * Updates the planet's display (x, y) coordinates.
     */
    public void updateDisplayPosition() {
        //TODO: Implement dynamic planet movement
    }

    /**
     * Draws the planet on the given Graphics context.
     * @param g The Graphics context to draw on.
     */
    public void draw(Graphics g) {
        prepareDisplayedIcon();

        if (displayedPlanetIcon != null) {
            int drawX = (int) (x - ICON_SIZE / 2.0);
            int drawY = (int) (y - ICON_SIZE / 2.0);
            g.drawImage(displayedPlanetIcon, drawX, drawY, ICON_SIZE, ICON_SIZE, null);
        } else {
            g.setColor(representativeColor); 
            g.fillOval((int) (x - radius), (int) (y - radius), radius * 2, radius * 2); 
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        String displayName = planetName + " (ID: " + planetId + ")";
        int textWidth = g.getFontMetrics().stringWidth(displayName);
        g.drawString(displayName, (int)x - textWidth / 2, (int)y + (ICON_SIZE / 2) + 15);


        if (isBoardingIndicatorActive) {
            g.setColor(Color.GREEN);
            g.drawOval((int) (x - ICON_SIZE/2) - 2, (int) (y - ICON_SIZE/2) - 2, ICON_SIZE + 2, ICON_SIZE + 2); 
            g.setFont(new Font("Arial", Font.BOLD, 12));
        String boardText = "Left-click to board";
            int boardTextWidth = g.getFontMetrics().stringWidth(boardText);
            g.drawString(boardText, (int)x - textWidth / 2, (int)y - (ICON_SIZE/2) - 10);
        }
    }

    /**
     * Checks if the ship is near the planet and updates the boarding indicator.
     */
    public boolean isNear(Ship ship, double proximityThreshold) {
        double distance = Math.sqrt(Math.pow(ship.getX() - this.x, 2) + Math.pow(ship.getY() - this.y, 2));
        isBoardingIndicatorActive = (distance < (ICON_SIZE / 2.0) + proximityThreshold);
        return isBoardingIndicatorActive;
    }

    public int getPlanetId() {
        return planetId;
    }

    public String getPlanetName() {
        return planetName;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(PlanetType planetType) {
        this.planetType = planetType;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getRadius() {
        return radius;
    }
    
    public Color getRepresentativeColor() {
        return representativeColor;
    }

    public void setRepresentativeColor(Color representativeColor) {
        this.representativeColor = representativeColor;
    }

    public int getGalaxyX() {
        return galaxyX;
    }

    public void setGalaxyX(int galaxyX) {
        this.galaxyX = galaxyX;
        this.x = galaxyX; 
    }

    public int getGalaxyY() {
        return galaxyY;
    }

    public void setGalaxyY(int galaxyY) {
        this.galaxyY = galaxyY;
        this.y = galaxyY; 
    }

    public Color getActiveHueShiftColor() {
        return activeHueShiftColor;
    }

    public void setActiveHueShiftColor(Color activeHueShiftColor) {
        this.activeHueShiftColor = activeHueShiftColor;
    }

    public Rectangle2D.Double getBounds() {
        double size = ICON_SIZE; 
        return new Rectangle2D.Double(this.x - size / 2, this.y - size / 2, size, size);
    }

    public boolean hasUpdate() {
        // For now, planets always "have" an update, can be refined later
        return true; 
    }

    public void update(double deltaTime) {
        // Placeholder for planet-specific update logic
        // For example, orbital rotation, resource generation, etc.
        updateDisplayPosition(); // Already exists, can be part of the update
    }
}
