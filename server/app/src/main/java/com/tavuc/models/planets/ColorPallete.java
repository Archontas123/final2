package com.tavuc.models.planets;

import java.awt.Color;

public class ColorPallete {

    private Color primarySurface;
    private Color primaryLiquid;
    private Color secondarySurface;
    private Color tertiarySurface;
    private Color hueShift;
    private Color rock;
    
    /**
     * Constructor for ColorPallete
     * @param primarySurface the primary surface color
     * @param primaryLiquid the primary liquid color
     * @param secondarySurface the secondary surface color
     * @param tertiarySurface the tertiary surface color
     * @param hueShift the hue shift color
     * @param rock the rock color
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
     * Gets the primary surface color
     * @return the primary surface color
     */
    public Color getPrimarySurface() {
        return primarySurface;
    }

    /**
     * Gets the primary liquid color
     * @return the primary liquid color
     */
    public Color getPrimaryLiquid() {
        return primaryLiquid;
    }

    /**
     * Gets the secondary surface color
     * @return the secondary surface color
     */
    public Color getSecondarySurface() {
        return secondarySurface;
    }

    /**
     * Gets the tertiary surface color
     * @return the tertiary surface color
     */
    public Color getTertiarySurface() {
        return tertiarySurface;
    }

    /**
     * Gets the hue shift color
     * @return the hue shift color
     */
    public Color getHueShift() {
        return hueShift;
    }

    /**
     * Gets the rock color
     * @return the rock color
     */
    public Color getRock() {
        return rock;
    }

    
}
