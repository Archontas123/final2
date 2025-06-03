package com.tavuc.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random; 
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.tavuc.exceptions.GameJoinException;
// import com.tavuc.managers.EmpireManager; // Removed
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Game;
import com.tavuc.models.planets.Moon;
import com.tavuc.models.planets.Planet;
import com.tavuc.models.planets.PlanetType;
import com.tavuc.networking.ClientSession;

public class LobbyManager {

    private final Map<Integer, GameManager> games = new ConcurrentHashMap<>();
    private final Map<String, GameManager> sessiontoGame = new ConcurrentHashMap<>();

    private final Map<Integer, Planet> planets = new ConcurrentHashMap<>(); 
    private int nextPlanetId = 1; 
    
    private NetworkManager networkManager; 
    // private EmpireManager empireManager; // Removed

    private static final int REGION_SIZE = 1000;
    private static final int PLANETS_PER_REGION_MIN = 2;
    private static final int PLANETS_PER_REGION_MAX = 5;
    private static final int MIN_PLANET_SEPARATION = 150; 
    private static final int MAX_PLACEMENT_ATTEMPTS = 20; 


    /**
     * Initializes the LobbyManager by generating initial planetary regions and the EmpireManager.
     * @param networkManager The global NetworkManager instance.
     */
    public void initializeLobby(NetworkManager networkManager) { 
        this.networkManager = networkManager; 
        System.out.println("Initializing LobbyManager and generating initial galaxy...");
        
        // this.empireManager = new EmpireManager(); // Removed
        // this.empireManager.initialize(this.networkManager); // Removed
        // System.out.println("Global EmpireManager initialized by LobbyManager."); // Removed

        Random random = new Random();
        int initialSpread = 5000; 
        for (int i = 0; i < 5; i++) { 
            int regionCenterX = random.nextInt(initialSpread * 2) - initialSpread;
            int regionCenterY = random.nextInt(initialSpread * 2) - initialSpread;
            int numPlanets = PLANETS_PER_REGION_MIN + random.nextInt(PLANETS_PER_REGION_MAX - PLANETS_PER_REGION_MIN + 1);
            generateGalaxyRegion(regionCenterX, regionCenterY, REGION_SIZE / 2, numPlanets);
        }
        System.out.println("LobbyManager initialized with " + planets.size() + " planets in " + "5 initial regions.");

    }

    public void update() { 
        // empireManager.update(); // Removed
            }

    private void createGameServiceForPlanet(Planet planet) {
        if (!games.containsKey(planet.getPlanetId())) {
            GameManager gameService = new GameManager();
            gameService.initialize(planet.getPlanetId(), planet, 10); 
            games.put(planet.getPlanetId(), gameService);
            System.out.println("Created initial game service for planet: " + planet.getName());
        }
    }


    /**
     * Returns a list of available games in the lobby.
     * @return List of Game objects representing available games.
     */
    public List<Game> getAvailableGames() {
        return games.values().stream()
            .map(gameService -> new Game(
                gameService.getGameId(),
                gameService.getPlanetName(),
                gameService.getPlanetName(), 
                gameService.getMaxPlayers(),
                gameService.getPlanet()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Allows a player to join a game (planet) if they are authenticated and not already in a game.
     * If a game instance for the planet doesn't exist, it's created.
     * @param session The client session of the player trying to join the game.
     * @param entityId The ID of the planet the player wants to join.
     * @return The GameService instance for the game the player joined.
     * @throws GameJoinException If the player cannot join the game due to various reasons.
     */
    public synchronized GameManager joinGame(ClientSession session, int entityId) throws GameJoinException {
        if (session == null || session.getAuthenticatedPlayer() == null) {
            throw new GameJoinException("Player must be authenticated to join a game.");
        }
        
        if (sessiontoGame.containsKey(session.getSessionId())) {
            throw new GameJoinException("Player is already in a game. Please leave the current game first.");
        }

        GameManager gameToJoin = games.get(entityId);

        if (gameToJoin == null) {
            Planet targetPlanet = planets.get(entityId);
      
            System.out.println("No active game for planet ID " + entityId + " (" + targetPlanet.getName() + "). Creating new GameService...");
            gameToJoin = new GameManager();
            int defaultMaxPlayers = 10; 
            gameToJoin.initialize(targetPlanet.getPlanetId(), targetPlanet, defaultMaxPlayers);
            games.put(targetPlanet.getPlanetId(), gameToJoin);
            System.out.println("Created and cached new GameService for planet: " + targetPlanet.getName());
        }

        Player player = session.getAuthenticatedPlayer();
        System.out.println("LobbyManager: Player " + player.getUsername() + " (Session: " + session.getSessionId() + ") attempting to join game " + entityId + " (" + gameToJoin.getPlanetName() + ")");
        boolean added = gameToJoin.addPlayer(player, session);

        if (added) {
            sessiontoGame.put(session.getSessionId(), gameToJoin);
            System.out.println("LobbyManager: Player " + player.getUsername() + " (Session: " + session.getSessionId() + ") successfully joined game " + entityId + " (" + gameToJoin.getPlanetName() + "). GameManager.addPlayer returned true.");
            return gameToJoin;
        } else {
            System.err.println("LobbyManager: Failed to add player " + player.getUsername() + " to game " + entityId + ". GameManager.addPlayer returned false.");
            throw new GameJoinException("Failed to add player " + player.getUsername() + " to game " + entityId + ". The game might be full or an internal error occurred.");
        }
    }

    /**
     * Allows a player to leave a game they are currently in.
     * @param session The client session of the player trying to leave the game.
     * @param gameId The ID of the game the player wants to leave.
     */
    public synchronized void leaveGame(ClientSession session, int gameId) {
        if (session == null) return;

        GameManager gameService = sessiontoGame.get(session.getSessionId());
        Player player = session.getAuthenticatedPlayer();

        if (gameService != null && player != null) {
            if (gameService.getGameId() == gameId) {
                gameService.removePlayer(player, session);
                sessiontoGame.remove(session.getSessionId());
                System.out.println("Player " + player.getUsername() + " (Session: " + session.getSessionId() + ") left game " + gameId);
            } else {
                System.err.println("Player " + player.getUsername() + " (Session: " + session.getSessionId() + ") tried to leave game " + gameId + " but is in game " + gameService.getGameId());
            }
        } else {
             System.err.println("Could not process leaveGame for session " + session.getSessionId() + " and game " + gameId + ". Player or game not found for this session.");
        }
    }
    
    /**
     * Gets the GameService instance for a specific game by its ID.
     * @param gameId The ID of the game.
     * @return The GameService instance for the specified game, or null if not found.
     */
    public GameManager getGameServiceById(int gameId) {
        return games.get(gameId);
    }

    /**
     * Gets a list of all active game services.
     * @return A list of GameService instances representing all active games.
     */
    public List<GameManager> getActiveGameServices() {
        return new ArrayList<>(games.values());
    }

    /**
     * Handles a client's request to get planets within a specific area of the galaxy.
     * This method will generate new planets/regions if needed and return them.
     *
     * @param galaxyCenterX The X-coordinate of the center of the requested area.
     * @param galaxyCenterY The Y-coordinate of the center of the requested area.
     * @param areaRadius    The radius of the requested area.
     * @return A list of Planet objects within or newly generated for the area.
     */
    public synchronized List<Planet> getPlanetsInArea(double galaxyCenterX, double galaxyCenterY, double areaRadius) {
        List<Planet> planetsInArea = new ArrayList<>();
        Random random = new Random();

        for (Planet planet : planets.values()) {
            double distanceSq = Math.pow(planet.getGalaxyX() - galaxyCenterX, 2) + Math.pow(planet.getGalaxyY() - galaxyCenterY, 2);
            if (distanceSq <= areaRadius * areaRadius) {
                planetsInArea.add(planet);
            }
        }
      
        if (planetsInArea.isEmpty()) { 
            System.out.println("Area is sparse. Generating a new planetary region near: " + galaxyCenterX + "," + galaxyCenterY);
            int numPlanets = PLANETS_PER_REGION_MIN + random.nextInt(PLANETS_PER_REGION_MAX - PLANETS_PER_REGION_MIN + 1);
            
            int actualRegionRadius = REGION_SIZE / 2;
            generateGalaxyRegion((int)galaxyCenterX, (int)galaxyCenterY, actualRegionRadius, numPlanets);
            
            for (Planet planet : planets.values()) {
                double distanceSq = Math.pow(planet.getGalaxyX() - galaxyCenterX, 2) + Math.pow(planet.getGalaxyY() - galaxyCenterY, 2);
                if (distanceSq <= areaRadius * areaRadius && !planetsInArea.contains(planet)) { 
                    planetsInArea.add(planet);
                }
            }
        }
        return planetsInArea;
    }

    private void generateGalaxyRegion(int regionCenterX, int regionCenterY, int regionRadius, int numPlanetsInRegion) {
        Random random = new Random();
        System.out.println("Generating " + numPlanetsInRegion + " planets in region: (" + regionCenterX + "," + regionCenterY + ") R:" + regionRadius);

        for (int i = 0; i < numPlanetsInRegion; i++) {
            int planetId = nextPlanetId++;
            String planetName = "Planet " + generateProceduralName(random) + "-" + planetId;
            PlanetType type = PlanetType.values()[random.nextInt(PlanetType.values().length)];
            
            int pX = 0;
            int pY = 0;
            boolean positionFound = false;
            int attempts = 0;
            Planet newPlanet = null; 

            while (!positionFound && attempts < MAX_PLACEMENT_ATTEMPTS) {
                attempts++;
                pX = regionCenterX - regionRadius + random.nextInt(regionRadius * 2);
                pY = regionCenterY - regionRadius + random.nextInt(regionRadius * 2);

     
                int tempWidth = 10 + random.nextInt(15);
                int tempHeight = 10 + random.nextInt(15);

                // Collision detection removed
                positionFound = true; // Assume position is always found
                newPlanet = new Planet(planetId, planetName, type,
                                           tempWidth, 
                                           tempHeight,
                                           0.5 + random.nextDouble(), 
                                           random.nextInt(300) - 150, 
                                           random.nextLong(), 
                                           pX, pY);
            }
            
            if (positionFound && newPlanet != null) {
                planets.put(newPlanet.getPlanetId(), newPlanet);
                System.out.println("Generated Planet: " + newPlanet.getName() + " at (" + newPlanet.getGalaxyX() + "," + newPlanet.getGalaxyY() + ")");
            } else {
                System.out.println("Could not find a non-overlapping position for a new planet in region (" + regionCenterX + "," + regionCenterY + ") after " + MAX_PLACEMENT_ATTEMPTS + " attempts. Skipping this planet.");
                nextPlanetId--; 
            }
        }
    }

    private String generateProceduralName(Random random) {
        String[] prefixes = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega", "Xylos", "Krypt", "Terra", "Aqua", "Ignis", "Ventus", "Cryo", "Geo", "Aero", "Helio", "Luna", "Sol", "Nova", "Orion", "Sirius", "Vega", "Rigel", "Cygnus", "Draco", "Lyra", "Corvus", "Hydra"};
        String[] suffixes = {"Prime", "Secundus", "Tertius", "Quartus", "Major", "Minor", "Proxima", "Centauri", "B", "C", "D", "System", "Colony", "Outpost", "Station", "Point", "Nebula", "Cluster", "Expanse", "Reach", "Void", "Core", "Rim", "Sector", "Quadrant"};
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)];
    }


    /**
     * Retrieves planets within a given area and formats them into a string
     * suitable for sending to the client.
     *
     * @param centerX The X-coordinate of the center of the area.
     * @param centerY The Y-coordinate of the center of the area.
     * @param radius The radius of the area.
     * @return A string formatted for the client, starting with "NEW_PLANETS_DATA ",
     *         or an empty string if no planets are found or an error occurs.
     */
    public String getPlanetsInAreaFormatted(double centerX, double centerY, double radius) {
        List<Planet> planets = getPlanetsInArea(centerX, centerY, radius);
        if (planets == null || planets.isEmpty()) {
            return "";
        }

        StringBuilder payloadBuilder = new StringBuilder();
        for (int i = 0; i < planets.size(); i++) {
            Planet planet = planets.get(i);
            payloadBuilder.append(planet.toClientStringFormat());
            if (i < planets.size() - 1) {
                payloadBuilder.append(";");
            }
        }
        return payloadBuilder.toString();
    }
}
