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


/**
 * Reperesnts a Planet in Space View
 */
public class Planet {

    /** The unique identifier for this planet. */
    private int planetId;
    /** The display name of the planet. */
    private String planetName;
    /** The type of the planet, which determines its base appearance. */
    private PlanetType planetType;
    /** The current x and y coordinates for rendering in the space view. */
    private double x, y; 
    /** The interaction radius of the planet. */
    private int radius; 
    /** A flag indicating if the "Press to board" indicator should be shown. */
    private boolean isBoardingIndicatorActive;
    /** A fallback color used for rendering if the icon cannot be loaded. */
    private Color representativeColor; 
    /** The color used to tint or "hue shift" the planet's base icon. */
    private Color activeHueShiftColor;
    /** The planet's static x-coordinate in the overall galaxy map. */
    private int galaxyX; 
    /** The planet's static y-coordinate in the overall galaxy map. */
    private int galaxyY; 
    /** The base icon loaded from resources, before any color modifications. */
    private transient BufferedImage basePlanetIcon;
    /** The final icon to be rendered, after applying the hue shift. */
    private transient BufferedImage displayedPlanetIcon;
    /** A flag to trigger the regeneration of the displayed icon when properties change. */
    private boolean iconNeedsUpdate = true;
    /** The standard size (width and height) for rendering the planet icon. */
    private static final int ICON_SIZE = 128;

    /**
     * Constructs a new client-side representation of a Planet.
     * @param planetId The unique identifier for the planet.
     * @param planetName The display name of the planet.
     * @param planetType The {@link PlanetType}, which determines the base icon.
     * @param radius The original radius from data (note: this is overridden by ICON_SIZE for rendering).
     * @param representativeColor A fallback color for display if the icon fails to load.
     * @param galaxyX The absolute x-coordinate of the planet in the galaxy.
     * @param galaxyY The absolute y-coordinate of the planet in the galaxy.
     * @param hueShiftColor The color to use for hue-shifting the base icon.
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
        prepareDisplayedIcon();
    }
    
    /**
     * Loads the base icon for the planet from application resources based on its type.
     */
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

    /**
     * Prepares the final, display-ready icon by loading the base icon and applying the hue shift.
     */
    private void prepareDisplayedIcon() {
        loadBaseIcon(); 
        if (basePlanetIcon != null) {
            displayedPlanetIcon = applyHueShift(basePlanetIcon, activeHueShiftColor);
        }
    }
        

    /**
     * Creates a new image by shifting the hue of a source image to match a target color's hue.
     * @param sourceImage The image to modify.
     * @param hueTargetColor The color from which to take the new hue.
     * @return A new {@link BufferedImage} with the applied hue shift.
     */
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
     * Placeholder method to update the planet's display coordinates, for future features like orbital motion.
     */
    public void updateDisplayPosition() {
        //TODO: Implement dynamic planet movement
    }

    /**
     * Draws the planet on the given Graphics context.
     * @param g The Graphics context to draw on.
     */
    public void draw(Graphics g) {
        if (iconNeedsUpdate || displayedPlanetIcon == null) {
            prepareDisplayedIcon();
            iconNeedsUpdate = false;
        }

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
     * Checks if a ship is within a specified proximity to the planet and updates the
     * boarding indicator state accordingly.
     * @param ship The ship to check the distance from.
     * @param proximityThreshold The additional distance beyond the planet's radius to consider "near".
     * @return {@code true} if the ship is near, {@code false} otherwise.
     */
    public boolean isNear(Ship ship, double proximityThreshold) {
        double distance = Math.sqrt(Math.pow(ship.getX() - this.x, 2) + Math.pow(ship.getY() - this.y, 2));
        isBoardingIndicatorActive = (distance < (ICON_SIZE / 2.0) + proximityThreshold);
        return isBoardingIndicatorActive;
    }

    /**
     * Gets the planet's unique ID.
     * @return The planet ID.
     */
    public int getPlanetId() {
        return planetId;
    }

    /**
     * Gets the planet's name.
     * @return The planet name.
     */
    public String getPlanetName() {
        return planetName;
    }

    /**
     * Gets the planet's type.
     * @return The {@link PlanetType}.
     */
    public PlanetType getPlanetType() {
        return planetType;
    }

    /**
     * Sets the planet's type and flags the icon for an update.
     * @param planetType The new {@link PlanetType}.
     */
    public void setPlanetType(PlanetType planetType) {
        if (this.planetType != planetType) {
            this.planetType = planetType;
            this.iconNeedsUpdate = true;
        }
    }

    /**
     * Gets the planet's current x-coordinate in the space view.
     * @return The x-coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the planet's current x-coordinate in the space view.
     * @param x The new x-coordinate.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets the planet's current y-coordinate in the space view.
     * @return The y-coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the planet's current y-coordinate in the space view.
     * @param y The new y-coordinate.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Gets the planet's interaction radius.
     * @return The radius.
     */
    public int getRadius() {
        return radius;
    }
    
    /**
     * Gets the planet's representative fallback color.
     * @return The representative {@link Color}.
     */
    public Color getRepresentativeColor() {
        return representativeColor;
    }

    /**
     * Sets the planet's representative fallback color.
     * @param representativeColor The new representative {@link Color}.
     */
    public void setRepresentativeColor(Color representativeColor) {
        this.representativeColor = representativeColor;
    }

    /**
     * Gets the planet's static x-coordinate in the galaxy.
     * @return The galaxy x-coordinate.
     */
    public int getGalaxyX() {
        return galaxyX;
    }

    /**
     * Sets the planet's static x-coordinate in the galaxy and updates its current display position.
     * @param galaxyX The new galaxy x-coordinate.
     */
    public void setGalaxyX(int galaxyX) {
        this.galaxyX = galaxyX;
        this.x = galaxyX; 
    }

    /**
     * Gets the planet's static y-coordinate in the galaxy.
     * @return The galaxy y-coordinate.
     */
    public int getGalaxyY() {
        return galaxyY;
    }

    /**
     * Sets the planet's static y-coordinate in the galaxy and updates its current display position.
     * @param galaxyY The new galaxy y-coordinate.
     */
    public void setGalaxyY(int galaxyY) {
        this.galaxyY = galaxyY;
        this.y = galaxyY; 
    }

    /**
     * Gets the color used for hue-shifting the planet's icon.
     * @return The active hue shift {@link Color}.
     */
    public Color getActiveHueShiftColor() {
        return activeHueShiftColor;
    }

    /**
     * Sets the color used for hue-shifting and flags the icon for an update.
     * @param activeHueShiftColor The new hue shift {@link Color}.
     */
    public void setActiveHueShiftColor(Color activeHueShiftColor) {
        if (this.activeHueShiftColor == null || !this.activeHueShiftColor.equals(activeHueShiftColor)) {
            this.activeHueShiftColor = activeHueShiftColor;
            this.iconNeedsUpdate = true;
        }
    }

    /**
     * Gets the bounding box for the planet's icon.
     * @return A {@link Rectangle2D.Double} representing the bounds.
     */
    public Rectangle2D.Double getBounds() {
        double size = ICON_SIZE; 
        return new Rectangle2D.Double(this.x - size / 2, this.y - size / 2, size, size);
    }

    /**
     * A method for performing per-frame updates on the planet's state.
     * @param deltaTime The time elapsed since the last frame, in seconds.
     */
    public void update(double deltaTime) {
        updateDisplayPosition();
    }

    /**
     * Gets the display size of the planet.
     * @return The size of the planet icon.
     */
    public int getSize() {
        return this.radius * 2; 
    }

    /**
     * Gets the base representative color of the planet.
     * @return The representative {@link Color}.
     */
    public Color getColor() {
        return this.representativeColor; 
    }
}