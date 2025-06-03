package com.tavuc.managers;

import com.tavuc.models.space.Space;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;
import com.tavuc.models.space.Moon;
import com.tavuc.models.planets.PlanetType;

import java.awt.Color;
import java.util.Collection; 
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class SpaceManager {
    private Space space;
    private long lastTickTime;
    private static final double VISUAL_SCALE = 2.0; 

    public SpaceManager() {
        this.space = new Space();
        this.lastTickTime = System.nanoTime();
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public void addPlanet(Planet planet) {
        this.space.addPlanet(planet);
    }

    public void removePlanet(Planet planet) {
        this.space.removePlanet(planet);
    }

    public void addShip(Ship ship) {
        this.space.addShip(ship);
    }

    public void removeShip(Ship ship) {
        this.space.removeShip(ship);
    }



    public Ship getPlayerShip() {
        if (space != null && !space.getShips().isEmpty()) {
            //TODO: UPDATE TO USE ID INSTEAD
            return space.getShips().get(0);
        }
        return null;
    }

    public void tick() {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastTickTime) / 1_000_000_000.0; 
        lastTickTime = currentTime;

        space.update(deltaTime);
    }

    public Planet getPlanetById(int planetId) {
        return space.getPlanets().stream().filter(p -> p.getPlanetId() == planetId).findFirst().orElse(null);
    }

    public Collection<Planet> getLoadedPlanets() {
        if (space == null) return java.util.Collections.emptyList();
        return space.getPlanets();
    }



    public void parseAndStorePlanetsData(String rawData) {
        System.out.println("SpaceManager: Parsing planets data: " + rawData);
        try {
            JsonObject jsonObject = JsonParser.parseString(rawData).getAsJsonObject();

            JsonArray planetsArray = jsonObject.getAsJsonArray("planets");

            for (JsonElement planet : planetsArray) {
                JsonObject planetJson = planet.getAsJsonObject();
                try {
                    int planetId = planetJson.get("gameId").getAsInt();
                    String planetName = planetJson.get("planetName").getAsString();
                    PlanetType planetType = PlanetType.valueOf(planetJson.get("type").getAsString()); 
                    int planetRadius = (int) (planetJson.get("size").getAsDouble() * VISUAL_SCALE);
                    int galaxyX = (int) planetJson.get("x").getAsDouble();
                    int galaxyY = (int) planetJson.get("y").getAsDouble();
                    Color planetColor = getDefaultColorForPlanetType(planetType); 
                    Color hueShiftColor = planetJson.has("hueShiftColor") ? new Color(planetJson.get("hueShiftColor").getAsInt(), true) : getDefaultColorForPlanetType(planetType);
                    Planet clientPlanet = new Planet(planetId, planetName, planetType, planetRadius, planetColor, galaxyX, galaxyY, hueShiftColor);
                    
                    this.addPlanet(clientPlanet); 
                    System.out.println("SpaceManager: Loaded planet: " + planetName + " (ID: " + planetId + ") at (" + galaxyX + "," + galaxyY + ")");

                } catch (Exception e) {
                    System.err.println("SpaceManager: Error parsing individual planet JSON object '" + planetJson.toString() + "': " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (JsonSyntaxException e) {
            System.err.println("SpaceManager: Error parsing planets JSON string: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("SpaceManager: Unexpected error processing planets data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Color getDefaultColorForPlanetType(PlanetType type) {
        switch (type) {
            case Desert: return new Color(210, 180, 140);
            case Gas: return new Color(255, 223, 186);   
            case Oceanic: return new Color(0, 105, 148);  
            case Cave: return new Color(101, 67, 33);    
            case Forest: return new Color(34, 139, 34);   
            case Ice: return new Color(173, 216, 230);   
            case Rocky: return new Color(169, 169, 169);  
            case Terrestrial: return new Color(144, 238, 144);
            case Volcanic: return new Color(255, 69, 0);   
            case Ecumenopoleis: return new Color(192, 192, 192);
            default: return Color.GRAY;
        }
    }
}
