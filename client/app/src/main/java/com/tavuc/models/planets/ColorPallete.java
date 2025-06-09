package com.tavuc.models.planets;

import java.awt.Color;

/**
 * Represents a collection of colors that define the visual theme for a planet or world.
 */
public class ColorPallete {

    /**
     * The main color for the planet's surface (e.g., grass, sand).
     */
    private Color primarySurface;
    /**
     * The main color for liquids on the planet (e.g., water, lava).
     */
    private Color primaryLiquid;
    /**
     * A secondary color for the planet's surface (e.g., dirt, soil).
     */
    private Color secondarySurface;
    /**
     * A tertiary color for the planet's surface, used for accents or less common features.
     */
    private Color tertiarySurface;
    /**
     * A color used for special hue-shifting effects or highlights.
     */
    private Color hueShift;
    /**
     * The color for rock formations and stone.
     */
    private Color rock;
    
    /**
     * Constructs a new ColorPallete with a complete set of thematic colors.
     * @param primarySurface The main color for the surface.
     * @param primaryLiquid The main color for liquids.
     * @param secondarySurface A secondary color for the surface.
     * @param tertiarySurface A tertiary color for the surface.
     * @param hueShift A color for special visual effects.
     * @param rock The color for rocks and stone.
     */
    public ColorPallete(Color primarySurface, Color primaryLiquid, Color secondarySurface, Color tertiarySurface, Color hueShift, Color rock) {
        this.primarySurface = primarySurface;   
        this.primaryLiquid = primaryLiquid;
        this.secondarySurface = secondarySurface;
        this.tertiarySurface = tertiarySurface;
        this.hueShift = hueShift;
        this.rock = rock;
    }

    /**
     * Gets the primary surface color.
     * @return The {@link Color} for the primary surface.
     */
    public Color getPrimarySurface() {
        return primarySurface;
    }

    /**
     * Gets the primary liquid color.
     * @return The {@link Color} for the primary liquid.
     */
    public Color getPrimaryLiquid() {
        return primaryLiquid;
    }

    /**
     * Gets the secondary surface color.
     * @return The {@link Color} for the secondary surface.
     */
    public Color getSecondarySurface() {
        return secondarySurface;
    }

    /**
     * Gets the tertiary surface color.
     * @return The {@link Color} for the tertiary surface.
     */
    public Color getTertiarySurface() {
        return tertiarySurface;
    }

    /**
     * Gets the hue shift color.
     * @return The {@link Color} for hue shifting effects.
     */
    public Color getHueShift() {
        return hueShift;
    }

    /**
     * Gets the rock color.
     * @return The {@link Color} for rocks.
     */
    public Color getRock() {
        return rock;
    }
}