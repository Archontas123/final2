package com.tavuc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tavuc.managers.WorldManager;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.networking.models.AttackShipFireBroadcast;
import com.tavuc.networking.models.AttackShipUpdateBroadcast;
import com.tavuc.networking.models.BaseMessage;
import com.tavuc.networking.models.CruiserUpdateBroadcast;
import com.tavuc.networking.models.EntityRemovedBroadcast;
import com.tavuc.networking.models.ErrorMessage;
import com.tavuc.networking.models.FireRequest;
import com.tavuc.networking.models.PlayerAttackRequest;
import com.tavuc.networking.models.PlayerShootRequest;
import com.tavuc.networking.models.GetPlayersRequest;
import com.tavuc.networking.models.GetPlayersResponse;
import com.tavuc.networking.models.JoinGameRequest;
import com.tavuc.networking.models.JoinGameResponse;
import com.tavuc.networking.models.LeaveGameRequest;
import com.tavuc.networking.models.ListGamesRequest;
import com.tavuc.networking.models.ListGamesResponse;
import com.tavuc.networking.models.LoginRequest;
import com.tavuc.networking.models.LoginResponse;
import com.tavuc.networking.models.PlayerJoinedBroadcast;
import com.tavuc.networking.models.PlayerLeftBroadcast;
import com.tavuc.networking.models.PlayerMovedBroadcast;
import com.tavuc.networking.models.PlayerUpdateRequest;
import com.tavuc.networking.models.PlayerInitialData;
import com.tavuc.networking.models.PlayerKilledBroadcast;
import com.tavuc.networking.models.PlayerDamagedBroadcast;
import com.tavuc.networking.models.ProjectileSpawnedBroadcast;
import com.tavuc.networking.models.ProjectileUpdateBroadcast;
import com.tavuc.networking.models.ProjectileRemovedBroadcast;
import com.tavuc.networking.models.RegisterRequest;
import com.tavuc.networking.models.RegisterResponse;
import com.tavuc.networking.models.RequestChunkRequest;
import com.tavuc.networking.models.RequestChunkResponse;
import com.tavuc.networking.models.RequestPaletteRequest;
import com.tavuc.networking.models.RequestPaletteResponse;
import com.tavuc.networking.models.RequestPlanetsAreaRequest;
import com.tavuc.networking.models.RequestPlanetsAreaResponse;
import com.tavuc.networking.models.ShipLeftBroadcast;
import com.tavuc.networking.models.ShipUpdateBroadcast;
import com.tavuc.networking.models.ShipDamagedBroadcast;
import com.tavuc.networking.models.ShipDestroyedBroadcast;
import com.tavuc.networking.models.ShipUpdateRequest;
import com.tavuc.ui.panels.GamePanel;
import com.tavuc.ui.panels.ISpacePanel;
import com.tavuc.ui.panels.SpacePanel;
import com.tavuc.ui.screens.GameScreen;
import com.tavuc.ui.screens.SpaceScreen;
import com.tavuc.ui.screens.StartScreen;
import com.tavuc.models.entities.Player;

import javax.swing.JFrame;
import java.awt.Window;


import java.awt.Color; 
public class Client {

    private static Client instance; 
    private boolean loggedInStatus = false; 
    private int playerId;
    private String username;
    private int currentGameId;
    private String currentPlanetName;
    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket socket;
    private static volatile CompletableFuture<String> activeRequestFuture; 
    public static GamePanel currentGamePanel = null;
    public static SpacePanel currentSpacePanel = null; 
    public static WorldManager worldManager = null;
    private static ColorPallete currentColorPalette = null;
    private static final Gson gson = new Gson();

    /**
     * Empty constructor for Client. 
     * This is private to enforce singleton pattern.
     */
    private Client(){}

    /**
     * Singleton instance getter for Client.
     * @return The singleton instance of Client
     */
    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    /**
     * Checks if the user is currently logged in.
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedInStatus;
    }

    /**
     * Gets the player ID of the logged-in user.
     * @return The player ID, or 0 if not logged in
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the username of the logged-in user.
     * @return The username, or null if not logged in
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the current game ID the user is in.
     * @return The current game ID, or 0 if not in a game
     */
    public int getCurrentGameId() {
        return currentGameId;
    }

    /**
     * Gets the name of the current planet the user is on.
     * @return The current planet name, or null if not in a game
     */
    public String getCurrentPlanetName() {
        return currentPlanetName;
    }

    /**
     * Sets the login details for the user.
     * @param username The username of the player
     * @param playerId The ID of the player
     */
    private void setLoginDetails(String username, int playerId) {
        this.username = username;
        this.playerId = playerId; 
    }

    /**
     * Sets the details of the planet the user has joined.
     * @param gameId The ID of the game (planet)
     * @param planetName The name of the planet
     */
    public void setJoinedPlanetDetails(int gameId, String planetName) {
        this.currentGameId = gameId;
        this.currentPlanetName = planetName;
    }


    /**
     * Waits for a response from the server. Will block until a response is received or the timeout is reached.
     * @param timeoutSeconds Timeout in seconds
     * @return The response from the server
     * @throws InterruptedException 
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private static String waitForResponse(long timeoutSeconds) throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<String> localRequestFuture = new CompletableFuture<>();
        activeRequestFuture = localRequestFuture; 

        try {
            return localRequestFuture.get(timeoutSeconds, TimeUnit.SECONDS);
        } finally {

            if (activeRequestFuture == localRequestFuture) {
                activeRequestFuture = null;
            }
        }
    }

    /**
     * Registers a new user with the server.
     * @param username Player's username
     * @param password Player's password
     * @return Server response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String register(String username, String password) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new RegisterResponse(false, "REGISTER_FAILED_NO_CONNECTION", null));
        RegisterRequest req = new RegisterRequest(username, password);
        out.println(gson.toJson(req));
        
        String jsonResponse = waitForResponse(20);
        RegisterResponse resp = gson.fromJson(jsonResponse, RegisterResponse.class);

        if (resp != null && resp.success) {
            if (resp.message != null && resp.message.startsWith("Login successful after registration")) {

                if (instance != null && resp.playerId != null) {
                    try {
                        instance.setLoginDetails(username, Integer.parseInt(resp.playerId));
                        instance.loggedInStatus = true;
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse player ID from register-login response: " + resp.playerId);
                    }
                }
            }
            return jsonResponse; 
        }
        return jsonResponse;
    }

    /**
     * Logs in an existing user.
     * @param username Player's username
     * @param password Player's password
     * @return Server response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String login(String username, String password) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) {
            if (instance != null) instance.loggedInStatus = false;
            return gson.toJson(new LoginResponse(false, "LOGIN_FAILED_NO_CONNECTION", null, null));
        }
        LoginRequest req = new LoginRequest(username, password);
        out.println(gson.toJson(req));

        String jsonResponse = waitForResponse(20);
        LoginResponse resp = gson.fromJson(jsonResponse, LoginResponse.class);

        if (resp != null && resp.success) {
            if (instance != null && resp.playerId != null) {
                try {
                    instance.setLoginDetails(username, Integer.parseInt(resp.playerId));
                    instance.loggedInStatus = true;
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse player ID from login response: " + resp.playerId);
                    if (instance != null) instance.loggedInStatus = false; 
                    return gson.toJson(new LoginResponse(false, "LOGIN_FAILED_INVALID_PLAYER_ID", null, null));
                }
            }
        } else {
            if (instance != null) instance.loggedInStatus = false;
        }
        return jsonResponse;
    }

    /**
     * Logs out the User.
     */
    public void logout() {
        this.loggedInStatus = false;
        this.username = null;
        this.playerId = 0;
        this.currentGameId = 0;
        this.currentPlanetName = null;
    }

    /**
     * Requests a list of available planets from the server.
     * @return Server response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String requestPlanetList() throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new ListGamesResponse(null)); 
        ListGamesRequest req = new ListGamesRequest();
        out.println(gson.toJson(req));
        return waitForResponse(10); 
    }

    /**
     * Joins a game with the specified ID.
     * @param gameId The ID of the game to join
     * @return Server response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String joinPlanet(int gameId, String planetName) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new JoinGameResponse(false, "JOIN_FAILED_NO_CONNECTION", String.valueOf(gameId), planetName, null));
        JoinGameRequest req = new JoinGameRequest(String.valueOf(gameId));
        out.println(gson.toJson(req));
        
        String jsonResponse = waitForResponse(20);
        JoinGameResponse resp = gson.fromJson(jsonResponse, JoinGameResponse.class);

        if (resp != null && resp.success) {
            if (instance != null) {
                instance.setJoinedPlanetDetails(gameId, planetName);
                 if (worldManager == null) {
                    worldManager = new WorldManager(gameId);
                    System.out.println("Client.joinPlanet: WorldManager initialized for game ID: " + gameId);
                } else {
                    worldManager.setGameId(gameId);
                    worldManager.clearChunks(); 
                    System.out.println("Client.joinPlanet: WorldManager reset for new game ID: " + gameId);
                }
                // Process initial player data from response
                if (resp.playersInGame != null) {
                    for (PlayerInitialData pData : resp.playersInGame) {
                        if (worldManager != null) {
                            PlayerJoinedBroadcast pjb = new PlayerJoinedBroadcast(pData.playerId, pData.username, pData.x, pData.y, pData.dx, pData.dy, pData.directionAngle);
                            worldManager.addPlayer(pjb);
                        }
                    }
                }
            }
        }
        return jsonResponse;
    }

    /**
     * Sends a message to the server to update the player's position.
     * @param playerId The ID of the player
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     * @param dx The change in x-coordinate
     * @param dy The change in y-coordinate
     * @param directionAngle The player's current direction angle in radians
     */
    public static void sendPlayerUpdate(int playerId, int x, int y, double dx, double dy, double directionAngle) {
        if (out == null) {
            System.err.println("Client not connected, cannot send player update.");
            return;
        }
        PlayerUpdateRequest req = new PlayerUpdateRequest(String.valueOf(playerId), x, y, dx, dy, directionAngle);
        out.println(gson.toJson(req));
    }

    /**
     * Requests the list of players in a game.
     * @param gameId The ID of the game
     * @return Server response
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String requestPlayers(int gameId) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new GetPlayersResponse(null)); 
        GetPlayersRequest req = new GetPlayersRequest(String.valueOf(gameId));
        out.println(gson.toJson(req));
        return waitForResponse(5); 
    }

    /**
     * Requests chunk data for a specific chunk in a game.
     * @param gameId The ID of the game
     * @param chunkX The x-coordinate of the chunk
     * @param chunkY The y-coordinate of the chunk
     * @return Server response containing the chunk data
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String requestChunkData(int gameId, int chunkX, int chunkY) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new RequestChunkResponse(chunkX, chunkY, null)); 
        RequestChunkRequest req = new RequestChunkRequest(String.valueOf(gameId), chunkX, chunkY);
        out.println(gson.toJson(req));
        return waitForResponse(10); 
    }

    /**
     * Requests the color palette for a specific planet.
     * @param gameId The ID of the game 
     * @return Server response containing the color palette data
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String requestPlanetPalette(int gameId) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new RequestPaletteResponse(null, null, null, null, null, null));
        RequestPaletteRequest req = new RequestPaletteRequest(String.valueOf(gameId));
        out.println(gson.toJson(req));
        
        String jsonResponse = waitForResponse(10);
        RequestPaletteResponse resp = gson.fromJson(jsonResponse, RequestPaletteResponse.class);
        if (resp != null && resp.primarySurfaceRGB != null) { // Check if response is valid
            processPlanetPaletteData(resp); 
        }
        return jsonResponse;
    }

    /**
     * Requests planet data for a specific area from the server.
     * @param centerX The X-coordinate of the center of the area
     * @param centerY The Y-coordinate of the center of the area
     * @param radius The radius of the area
     * @return Server response containing the planets data
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public static String requestPlanetsArea(double centerX, double centerY, double radius) throws InterruptedException, ExecutionException, TimeoutException {
        if (out == null) return gson.toJson(new RequestPlanetsAreaResponse(null));
        RequestPlanetsAreaRequest req = new RequestPlanetsAreaRequest(centerX, centerY, radius);
        out.println(gson.toJson(req));
        return waitForResponse(10);
    }

    public static void sendShipUpdate(int playerId, double x, double y, double angle, double dx, double dy, boolean thrusting) {
        if (out == null) {
            System.err.println("Client not connected, cannot send ship update.");
            return;
        }
        ShipUpdateRequest req = new ShipUpdateRequest(String.valueOf(playerId), x, y, angle, dx, dy, thrusting);
        out.println(gson.toJson(req));
    }

    /**
     * Sends a command to the server indicating the player is leaving the current game.
     * @param gameId The ID of the game the player is leaving.
     */
    public static void sendLeaveGameCommand(int gameId) {
        if (out == null) {
            System.err.println("Client not connected, cannot send leave game command.");
            return;
        }
        LeaveGameRequest req = new LeaveGameRequest(); 
        out.println(gson.toJson(req));
        System.out.println("Client: Sent LEAVE_GAME_REQUEST");
    }

    /**
     * Sends a fire request to the server.
     * @param request The FireRequest object
     */
    public static void sendFireRequest(FireRequest request) {
        if (out == null) {
            System.err.println("Client not connected, cannot send fire request.");
            return;
        }
        try {
            out.println(new Gson().toJson(request));
        } catch (Exception e) {
            System.err.println("Error sending fire request: " + e.getMessage());
        }
    }

    /**
     * Sends a player attack request to the server.
     * @param attackerId ID of the attacking player
     * @param targetId   ID of the target player
     */
    public static void sendPlayerAttack(int attackerId, int targetId) {
        if (out == null) {
            System.err.println("Client not connected, cannot send attack request.");
            return;
        }
        System.out.println("[Client] Sending attack from " + attackerId + " to " + targetId
                + " at " + System.currentTimeMillis());
        PlayerAttackRequest req = new PlayerAttackRequest(
                String.valueOf(attackerId),
                String.valueOf(targetId),
                0.0
        );
        out.println(gson.toJson(req));
    }

    /**
     * Sends a player shoot request to the server.
     */
    public static void sendPlayerShoot(int playerId, double x, double y, double direction) {
        if (out == null) {
            System.err.println("Client not connected, cannot send shoot request.");
            return;
        }
        PlayerShootRequest req = new PlayerShootRequest(String.valueOf(playerId), x, y, direction);
        out.println(gson.toJson(req));
    }



    /**
     * Gets the current color palette.
     * @return The current color palette, or null if not set
     */
    public static ColorPallete getCurrentColorPalette() {
        return currentColorPalette;
    }

    /**
     * Starts the server listener thread.
     */
    public static void startServerListener() {
        new Thread(() -> {
            try {
                String jsonFromServer;
                while (socket != null && !socket.isClosed() && (jsonFromServer = in.readLine()) != null) {
                    //System.out.println("Server JSON: " + jsonFromServer); 
                    
                    String processedJson = jsonFromServer; 
                    if (jsonFromServer.startsWith("\"") && jsonFromServer.endsWith("\"")) {
                        try {
                            String unwrappedJson = gson.fromJson(jsonFromServer, String.class);
                            if (unwrappedJson != null && 
                                ((unwrappedJson.trim().startsWith("{") && unwrappedJson.trim().endsWith("}")) || 
                                 (unwrappedJson.trim().startsWith("[") && unwrappedJson.trim().endsWith("]")))) {
                                processedJson = unwrappedJson;
                                System.out.println("Listener: Unwrapped JSON string: " + processedJson);
                            } else {
                                System.out.println("Listener: Unwrapping attempt did not yield object/array-like JSON, using original: " + jsonFromServer + (unwrappedJson == null ? " (unwrapped to null)" : " (unwrapped: " + unwrappedJson + ")"));
                            }
                        } catch (JsonSyntaxException e_unwrap) {
                   
                            System.out.println("Listener: Failed to unwrap as JSON String (Error: " + e_unwrap.getMessage() + "), using original: " + jsonFromServer);
                        }
                    }

                    BaseMessage baseMsg = null;
                    try {
                        baseMsg = gson.fromJson(processedJson, BaseMessage.class);
                    } catch (JsonSyntaxException e_base) {
                        System.err.println("Listener: Received invalid JSON structure for BaseMessage (after potential unwrap): " + processedJson + " - Error: " + e_base.getMessage());
    
                        continue; 
                    }

                    if (baseMsg == null || baseMsg.type == null) {
                        System.err.println("Listener: Received message with no type (after potential unwrap): " + processedJson);
                        continue;
                    }

                    boolean handledByFuture = false;
                    if (activeRequestFuture != null && !activeRequestFuture.isDone()) {
                        if (!baseMsg.type.endsWith("_BROADCAST") && !baseMsg.type.equals("ERROR_MESSAGE")) {
                             activeRequestFuture.complete(processedJson); 
                             handledByFuture = true;
                        } else if (baseMsg.type.equals("ERROR_MESSAGE")) { 
                            activeRequestFuture.complete(processedJson); 
                            handledByFuture = true;
                        }
                    }

                    if (handledByFuture) {
                        if ("REQUEST_CHUNK_RESPONSE".equals(baseMsg.type) && worldManager != null) {
                            RequestChunkResponse chunkResponse = gson.fromJson(processedJson, RequestChunkResponse.class); 
                            SwingUtilities.invokeLater(() -> worldManager.processChunkData(chunkResponse));
                        } else if ("REQUEST_PALETTE_RESPONSE".equals(baseMsg.type)) {
                            RequestPaletteResponse paletteResponse = gson.fromJson(processedJson, RequestPaletteResponse.class); 
                            processPlanetPaletteData(paletteResponse);
                        }
                    } else {
                        switch (baseMsg.type) {
                            case "PLAYER_MOVED_BROADCAST":
                                if (worldManager != null) {
                                    PlayerMovedBroadcast event = gson.fromJson(processedJson, PlayerMovedBroadcast.class);
                                    SwingUtilities.invokeLater(() -> worldManager.updatePlayer(event));
                                } else if (currentGamePanel != null) { // Fallback or alternative handler
                                    PlayerMovedBroadcast event = gson.fromJson(processedJson, PlayerMovedBroadcast.class);
                                    // Assuming GamePanel might have a similar method or WorldManager is preferred
                                    // currentGamePanel.processPlayerUpdate(event); // Or adapt as needed
                                    System.out.println("Client: PLAYER_MOVED_BROADCAST received, currentGamePanel to handle (if worldManager is null)");
                                }
                                break;
                            case "PLAYER_JOINED_BROADCAST":
                                System.out.println("Listener: Received PLAYER_JOINED_BROADCAST. JSON: " + processedJson);
                                if (worldManager != null) {
                                    System.out.println("Listener: worldManager is NOT null. Processing PLAYER_JOINED_BROADCAST.");
                                    PlayerJoinedBroadcast joinedEvent = gson.fromJson(processedJson, PlayerJoinedBroadcast.class);
                                    System.out.println("Listener: Parsed PlayerJoinedBroadcast for player ID: " + joinedEvent.playerId + ", username: " + joinedEvent.username);
                                    SwingUtilities.invokeLater(() -> {
                                        System.out.println("Listener (invokeLater): Calling worldManager.addPlayer for " + joinedEvent.username);
                                        worldManager.addPlayer(joinedEvent);
                                    });
                                } else {
                                    System.err.println("Listener: worldManager IS NULL when PLAYER_JOINED_BROADCAST received. Cannot process. JSON: " + processedJson);
                                }
                                break;
                            case "PLAYER_LEFT_BROADCAST":
                                if (worldManager != null) {
                                    PlayerLeftBroadcast leftEvent = gson.fromJson(processedJson, PlayerLeftBroadcast.class);
                                    SwingUtilities.invokeLater(() -> worldManager.removePlayer(leftEvent.playerId));
                                }
                                break;
                            case "SHIP_UPDATE_BROADCAST":
                                if (currentSpacePanel != null) {
                                    ShipUpdateBroadcast event = gson.fromJson(processedJson, ShipUpdateBroadcast.class); 
                                    try {
                                        int pId = Integer.parseInt(event.playerId);
                                        SwingUtilities.invokeLater(() -> currentSpacePanel.updateOtherShip(pId, event.x, event.y, event.angle, event.dx, event.dy, event.thrusting));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Listener: Error parsing playerId for SHIP_UPDATE_BROADCAST: " + event.playerId);
                                    }
                                }
                                break;
                            case "SHIP_LEFT_BROADCAST":
                                if (currentSpacePanel != null) {
                                    ShipLeftBroadcast event = gson.fromJson(processedJson, ShipLeftBroadcast.class);
                                    SwingUtilities.invokeLater(() -> currentSpacePanel.removeOtherShip(Integer.parseInt(event.playerId)));
                                }
                                break;
                            case "SHIP_DAMAGED_BROADCAST":
                                if (currentSpacePanel != null) {
                                    ShipDamagedBroadcast dmgEvent = gson.fromJson(processedJson, ShipDamagedBroadcast.class);
                                    SwingUtilities.invokeLater(() -> currentSpacePanel.handleShipDamaged(dmgEvent));
                                }
                                break;
                            case "SHIP_DESTROYED_BROADCAST":
                                if (currentSpacePanel != null) {
                                    ShipDestroyedBroadcast destroyedEvent = gson.fromJson(processedJson, ShipDestroyedBroadcast.class);
                                    SwingUtilities.invokeLater(() -> currentSpacePanel.handleShipDestroyed(destroyedEvent));
                                }
                                break;
                            case "PLAYER_DAMAGED_BROADCAST":
                                PlayerDamagedBroadcast pdEvent = gson.fromJson(processedJson, PlayerDamagedBroadcast.class);
                                SwingUtilities.invokeLater(() -> {
                                    int id;
                                    try {
                                        id = Integer.parseInt(pdEvent.playerId);
                                    } catch (NumberFormatException ex) {
                                        return;
                                    }
                                    if (currentGamePanel != null && currentGamePanel.getPlayer().getPlayerId() == id) {
                                        Player p = currentGamePanel.getPlayer();
                                        p.setHealth(pdEvent.currentHealth);
                                        p.triggerDamageEffect();
                                        currentGamePanel.showPlayerDamage(id, pdEvent.damage);
                                    } else if (worldManager != null) {
                                        Player other = worldManager.getOtherPlayer(id);
                                        if (other != null) {
                                            other.setHealth(pdEvent.currentHealth);
                                            other.triggerDamageEffect();
                                            if (currentGamePanel != null) {
                                                currentGamePanel.showPlayerDamage(id, pdEvent.damage);
                                            }
                                        }
                                    }
                                });
                                break;
                            case "PLAYER_KILLED_BROADCAST":
                                PlayerKilledBroadcast killedEvent = gson.fromJson(processedJson, PlayerKilledBroadcast.class);
                                System.out.println("Player " + killedEvent.playerId + " was killed by " + killedEvent.killerId);
                                try {
                                    int killedId = Integer.parseInt(killedEvent.playerId);
                                    if (killedId == getInstance().getPlayerId()) {
                                        SwingUtilities.invokeLater(Client::returnToShip);
                                    } else if (worldManager != null) {
                                        SwingUtilities.invokeLater(() -> worldManager.removePlayer(killedEvent.playerId));
                                    }
                                } catch (NumberFormatException ignore) {
                                }
                                break;
                            case "PROJECTILE_SPAWNED_BROADCAST":
                                ProjectileSpawnedBroadcast spawnEvent = gson.fromJson(processedJson, ProjectileSpawnedBroadcast.class);
                                if (currentSpacePanel != null) {
                                    SwingUtilities.invokeLater(() -> currentSpacePanel.handleProjectileSpawned(spawnEvent));
                                } else if (currentGamePanel != null) {
                                    SwingUtilities.invokeLater(() -> currentGamePanel.handleProjectileSpawned(spawnEvent));
                                }
                                break;
                            case "PROJECTILE_UPDATE_BROADCAST":
                                ProjectileUpdateBroadcast upEvent = gson.fromJson(processedJson, ProjectileUpdateBroadcast.class);
                                if (currentSpacePanel != null) {
                                    SwingUtilities.invokeLater(() -> currentSpacePanel.handleProjectileUpdate(upEvent));
                                } else if (currentGamePanel != null) {
                                    SwingUtilities.invokeLater(() -> currentGamePanel.handleProjectileUpdate(upEvent));
                                }
                                break;
                            case "PROJECTILE_REMOVED_BROADCAST":
                                ProjectileRemovedBroadcast rmEvent = gson.fromJson(processedJson, ProjectileRemovedBroadcast.class);
                                if (currentSpacePanel != null) {
                                    SwingUtilities.invokeLater(() -> currentSpacePanel.handleProjectileRemoved(rmEvent));
                                } else if (currentGamePanel != null) {
                                    SwingUtilities.invokeLater(() -> currentGamePanel.handleProjectileRemoved(rmEvent));
                                }
                                break;
                            case "REQUEST_CHUNK_RESPONSE":
                                if (worldManager != null) {
                                    RequestChunkResponse chunkResponse = gson.fromJson(processedJson, RequestChunkResponse.class);
                                    SwingUtilities.invokeLater(() -> worldManager.processChunkData(chunkResponse));
                                }
                                break;
                            case "REQUEST_PALETTE_RESPONSE": 
                                 RequestPaletteResponse paletteResponse = gson.fromJson(processedJson, RequestPaletteResponse.class);
                                 processPlanetPaletteData(paletteResponse);
                                break;
                           
                            case "ERROR_MESSAGE":
                                ErrorMessage errMsg = gson.fromJson(processedJson, ErrorMessage.class); 
                                System.err.println("Listener: Received ERROR_MESSAGE from server: " + errMsg.errorMessageContent);
                                break;
                            default:
                                System.out.println("Listener: Unhandled broadcast/async message type: " + baseMsg.type);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                if (socket != null && !socket.isClosed()) {
                    System.err.println("Listener: Lost connection or read error: " + e.getMessage());
                    if (activeRequestFuture != null && !activeRequestFuture.isDone()) { 
                        activeRequestFuture.completeExceptionally(e);
                    }
                }
            } finally {
                System.out.println("Server listener thread stopped.");
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (socket != null) socket.close();
                } catch (IOException ex) {
                    System.err.println("Listener: Error closing resources: " + ex.getMessage());
                }
            }
        }, "Client-ServerListener").start();
    }

    /**
     * Processes the PLANET_PALETTE message from the server.
     * @param paletteResponse The RequestPaletteResponse object
     */
    private static void processPlanetPaletteData(RequestPaletteResponse paletteResponse) {
        System.out.println("Processing PLANET_PALETTE response");
        if (paletteResponse == null) {
            System.err.println("Received null palette response.");
            return;
        }
        try {
            Color primarySurface = parseColor(paletteResponse.primarySurfaceRGB);
            Color primaryLiquid = parseColor(paletteResponse.primaryLiquidRGB);
            Color secondarySurface = parseColor(paletteResponse.secondarySurfaceRGB);
            Color tertiarySurface = parseColor(paletteResponse.tertiarySurfaceRGB);
            Color hueShift = parseColor(paletteResponse.hueShiftRGB);
            Color rock = parseColor(paletteResponse.rockRGB);
            currentColorPalette = new ColorPallete(primarySurface, primaryLiquid, secondarySurface, tertiarySurface, hueShift, rock);
            System.out.println("Successfully processed planet palette from JSON.");
            if (worldManager != null) {
                worldManager.setPallete(currentColorPalette);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing PLANET_PALETTE colors from JSON: " + e.getMessage());
            currentColorPalette = null;
        }
    }

    /**
     * Parses a color from an RGB string in the format "R,G,B".
     * @param rgbString The RGB string to parse
     * @return The parsed Color object
     * @throws IllegalArgumentException If the format is invalid or contains non-numeric values
     */
    private static Color parseColor(String rgbString) throws IllegalArgumentException {
        String[] components = rgbString.split(",");
        if (components.length != 3) {
            throw new IllegalArgumentException("Invalid RGB string format: " + rgbString);
        }
        try {
            int r = Integer.parseInt(components[0].trim());
            int g = Integer.parseInt(components[1].trim());
            int b = Integer.parseInt(components[2].trim());
            return new Color(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in RGB string: " + rgbString, e);
        }
    }

    /**
     * Handles the logic to return the player to the ship screen.
     */
    public static void returnToShip() {
        SwingUtilities.invokeLater(() -> {
            if (instance == null) {
                System.err.println("Client instance is null, cannot return to ship.");
                return;
            }

            int gameIdToLeave = instance.currentGameId; 

            if (currentGamePanel != null) {
                currentGamePanel.stopGame();
                currentGamePanel = null;
            }
            if (currentSpacePanel != null) {
                currentSpacePanel = null;
            }
            if (worldManager != null) {
                worldManager.clearChunks(); 
                worldManager = null;
            }

            if (gameIdToLeave != 0) {
                sendLeaveGameCommand(gameIdToLeave);
            }
            
            instance.currentGameId = 0; 
            instance.currentPlanetName = null;
            currentColorPalette = null;

            JFrame currentFrame = null;
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof GameScreen && window.isVisible()) {
                    currentFrame = (JFrame) window;
                    break;
                }
            }

            if (currentFrame != null) {
                currentFrame.dispose();
            } else {
                System.err.println("Could not find the current GameScreen to dispose.");
            }
            
            SpaceScreen ss = new SpaceScreen(null, instance.getPlayerId(), instance.getUsername()); 

            System.out.println("Returned to Space Screen (Space Navigation).");
        });
    }

    public static void main(String[] args) {
        instance = getInstance();

        try {
            socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to server");
            startServerListener();
            SwingUtilities.invokeLater(() -> new StartScreen());
        } catch (IOException e) {
            System.err.println("Could not connect to the server: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Could not connect to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            // Optionally, still show StartScreen in a disconnected state or exit
            SwingUtilities.invokeLater(() -> new StartScreen()); // Or handle error differently
        }
    }
}
