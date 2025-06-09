package com.tavuc.networking.models;
/**
 * Represents the RequestPlanetsAreaResponse networking message.
 */

import java.util.List;

public class RequestPlanetsAreaResponse extends BaseMessage {
    public List<PlanetInfo> planets;

    public RequestPlanetsAreaResponse() {
    }

    public RequestPlanetsAreaResponse(List<PlanetInfo> planets) {
        this.type = "REQUEST_PLANETS_AREA_RESPONSE";
        this.planets = planets;
    }
}
