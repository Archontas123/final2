package com.tavuc.networking.models;
/**
 * Represents the RequestPaletteResponse networking message.
 */

public class RequestPaletteResponse extends BaseMessage {
    public String primarySurfaceRGB;
    public String primaryLiquidRGB;
    public String secondarySurfaceRGB;
    public String tertiarySurfaceRGB;
    public String hueShiftRGB;
    public String rockRGB;

    /**
     * Constructs a new RequestPaletteResponse.
     */
    public RequestPaletteResponse() {
    }

    /**
     * Constructs a new RequestPaletteResponse.
     */
    public RequestPaletteResponse(String primarySurfaceRGB, String primaryLiquidRGB, String secondarySurfaceRGB, String tertiarySurfaceRGB, String hueShiftRGB, String rockRGB) {
        this.type = "REQUEST_PALETTE_RESPONSE";
        this.primarySurfaceRGB = primarySurfaceRGB;
        this.primaryLiquidRGB = primaryLiquidRGB;
        this.secondarySurfaceRGB = secondarySurfaceRGB;
        this.tertiarySurfaceRGB = tertiarySurfaceRGB;
        this.hueShiftRGB = hueShiftRGB;
        this.rockRGB = rockRGB;
    }
}
