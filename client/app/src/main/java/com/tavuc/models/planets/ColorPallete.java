package com.tavuc.models.planets;

import java.awt.Color;

public class ColorPallete {

    private Color primarySurface;
    private Color primaryLiquid;
    private Color secondarySurface;
    private Color tertiarySurface;
    private Color hueShift;
    private Color rock;
    
    public ColorPallete(Color primarySurface, Color primaryLiquid, Color secondarySurface, Color tertiarySurface, Color hueShift, Color rock) {
        this.primarySurface = primarySurface;   
        this.primaryLiquid = primaryLiquid;
        this.secondarySurface = secondarySurface;
        this.tertiarySurface = tertiarySurface;
        this.hueShift = hueShift;
        this.rock = rock;
    }

    public Color getPrimarySurface() {
        return primarySurface;
    }

    public Color getPrimaryLiquid() {
        return primaryLiquid;
    }

    public Color getSecondarySurface() {
        return secondarySurface;
    }

    public Color getTertiarySurface() {
        return tertiarySurface;
    }

    public Color getHueShift() {
        return hueShift;
    }

    public Color getRock() {
        return rock;
    }
}
