package com.tavuc.managers;

import com.tavuc.models.space.Space;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;
import com.tavuc.models.planets.PlanetType;

import java.awt.Color;
import java.util.Collection; 
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Manages the client-side state of the game's space environment. 
 */
public class SpaceManager {
    /**
     * The main {@link Space} instance that contains all celestial bodies and ships.
     */
    private Space space;
    /**
     * Timestamp of the last tick, used to calculate delta time for frame-rate independent updates.
     */
    private long lastTickTime;
    /**
     * A scaling factor applied to planet sizes for better visual representation on screen.
     */
    private static final double VISUAL_SCALE = 2.0; 

    /**
     * Constructs a new SpaceManager, initializing an empty {@link Space} and the tick timer.
     */
    public SpaceManager() {
        this.space = new Space();
        this.lastTickTime = System.nanoTime();
    }

    /**
     * Gets the {@link Space} object managed by this manager.
     * @return The current Space instance.
     */
    public Space getSpace() {
        return space;
    }

    /**
     * Sets the {@link Space} object to be managed by this manager.
     * @param space The new Space instance to manage.
     */
    public void setSpace(Space space) {
        this.space = space;
    }

    /**
     * Adds a {@link Planet} to the managed space.
     * @param planet The planet to add.
     */
    public void addPlanet(Planet planet) {
        this.space.addPlanet(planet);
    }

    /**
     * Removes a {@link Planet} from the managed space.
     * @param planet The planet to remove.
     */
    public void removePlanet(Planet planet) {
        this.space.removePlanet(planet);
    }

    /**
     * Adds a {@link Ship} to the managed space.
     * @param ship The ship to add.
     */
    public void addShip(Ship ship) {
        this.space.addShip(ship);
    }

    /**
     * Removes a {@link Ship} from the managed space.
     * @param ship The ship to remove.
     */
    public void removeShip(Ship ship) {
        this.space.removeShip(ship);
    }



    /**
     * Retrieves the player's ship from the space.
     * @return The player's {@link Ship} object, or {@code null} if no ships are present.
     */
    public Ship getPlayerShip() {
        if (space != null && !space.getShips().isEmpty()) {
            //TODO: UPDATE TO USE ID INSTEAD
            return space.getShips().get(0);
        }
        return null;
    }

    /**
     * Performs a single update step for the entire space simulation.
     */
    public void tick() {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastTickTime) / 1_000_000_000.0; 
        lastTickTime = currentTime;

        space.update(deltaTime);
    }

    /**
     * Finds and returns a {@link Planet} from the managed space by its unique ID.
     * @param planetId The ID of the planet to find.
     * @return The matching {@link Planet} object, or {@code null} if no planet with that ID is found.
     */
    public Planet getPlanetById(int planetId) {
        return space.getPlanets().stream().filter(p -> p.getPlanetId() == planetId).findFirst().orElse(null);
    }

    /**
     * Gets a collection of all planets currently loaded in the managed {@link Space}.
     * @return A collection of all loaded planets. Returns an empty collection if space is not initialized.
     */
    public Collection<Planet> getLoadedPlanets() {
        if (space == null) return java.util.Collections.emptyList();
        return space.getPlanets();
    }



    /**
     * Parses a JSON string containing an array of planet data, creates {@link Planet}
     * objects from it, and adds them to the managed space. 
     * @param rawData The raw JSON string containing planet data.
     */
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

    /**
     * Provides a default {@link Color} for a given {@link PlanetType}. 
     * @param type The {@link PlanetType} for which to get a color.
     * @return The default color associated with the planet type.
     */
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