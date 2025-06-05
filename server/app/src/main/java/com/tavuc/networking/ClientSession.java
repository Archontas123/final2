package com.tavuc.networking;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.tavuc.networking.models.*; 
import com.tavuc.networking.models.ShipUpdateRequest;
import com.tavuc.managers.CombatManager;
import com.tavuc.exceptions.AuthenticationException;
import com.tavuc.exceptions.GameJoinException;
import com.tavuc.exceptions.RegistrationException;
import com.tavuc.managers.AuthManager;
import com.tavuc.managers.GameManager;
import com.tavuc.managers.LobbyManager;
import com.tavuc.managers.NetworkManager;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.Game;
import com.tavuc.models.planets.Tile;
import com.tavuc.models.space.PlayerShip;
import com.tavuc.models.space.Ship;

public class ClientSession implements Runnable {

    private final Socket clientSocket;
    private final AuthManager authService;
    private final Gson gson = new Gson();
    private final LobbyManager lobbyService;
    private NetworkManager networkManager; 
    private GameManager currentGameService;
    private PrintWriter out;
    private BufferedReader in;
    private Player player;
    private final String sessionId;
    private volatile boolean running = true;
    private ClientSessionListener sessionListener;

    /**
     * Constructor for ClientSession
     * @param socket The client socket for this session
     * @param authService The authentication service to handle user authentication
     * @param lobbyService The game lobby service to manage game sessions
     */
    public ClientSession(Socket socket, AuthManager authService, LobbyManager lobbyService) {
        this.clientSocket = socket;
        this.authService = authService;
        this.lobbyService = lobbyService;
        this.sessionId = UUID.randomUUID().toString();
        this.currentGameService = null;
    }

    public ClientSession(Socket socket, AuthManager authService, LobbyManager lobbyService, NetworkManager networkManager) {
        this.clientSocket = socket;
        this.authService = authService;
        this.lobbyService = lobbyService;
        this.networkManager = networkManager; 
        this.sessionId = UUID.randomUUID().toString();
        this.currentGameService = null;
    }

    /**
     * Starts the client session in a new thread.
     */
    public void start() {
        new Thread(this).start();
    }

    /**
     * The main run method for the client session.
     * It handles reading messages from the client and processing them.
     */
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Client session " + sessionId + " started for " + clientSocket.getInetAddress().getHostAddress());

            String message;
            while (running && (message = in.readLine()) != null) {
                processMessage(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("IOException in session " + sessionId + ": " + e.getMessage());
            }
        } finally {
            close("Client disconnected or error");
        }
    }

    /**
     * Processes incoming messages from the client.
     * @param jsonMessage The raw message received from the client.
     */
    public void processMessage(String jsonMessage) {
        try {
            BaseMessage baseMsg = gson.fromJson(jsonMessage, BaseMessage.class);
            String messageType = baseMsg.type;

            if (!isAuthenticated() && !("REGISTER_REQUEST".equals(messageType) || "LOGIN_REQUEST".equals(messageType))) {
                sendMessage(gson.toJson(new ErrorMessage("Not authenticated. Please login or register.")));
                return;
            }

            switch (messageType) {
                case "REGISTER_REQUEST":
                    handleRegisterCommand(jsonMessage);
                    break;
                case "LOGIN_REQUEST":
                    handleLoginCommand(jsonMessage);
                    break;
                case "LIST_GAMES_REQUEST":
                    handleListGamesCommand();
                    break;
                case "JOIN_GAME_REQUEST":
                    handleJoinGameCommand(jsonMessage);
                    break;
                case "LEAVE_GAME_REQUEST":
                    handleLeaveGameCommand();
                    break;
                case "PLAYER_UPDATE_REQUEST":
                    handleUpdatePlayerCommand(jsonMessage);
                    break;
                case "GET_PLAYERS_REQUEST":
                    handleGetPlayersCommand(jsonMessage);
                    break;
                case "REQUEST_CHUNK_REQUEST":
                    handleRequestChunkCommand(jsonMessage);
                    break;
                case "REQUEST_PALETTE_REQUEST":
                    handleRequestPaletteCommand(jsonMessage);
                    break;
                case "REQUEST_PLANETS_AREA_REQUEST":
                    handleRequestPlanetsAreaCommand(jsonMessage);
                    break;
                case "SHIP_UPDATE_REQUEST":
                    handleShipUpdateCommand(jsonMessage);
                    break;
                case "ATTACK_REQUEST":
                    handleAttackRequest(jsonMessage);
                    break;
                case "FIRE_REQUEST":
                    handleFireRequest(jsonMessage);
                    break;
                default:
                    sendMessage(gson.toJson(new ErrorMessage("UNKNOWN_COMMAND " + messageType)));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing command '" + jsonMessage + "' in session " + sessionId + ": " + e.getMessage());
            e.printStackTrace();
            sendMessage(gson.toJson(new ErrorMessage("Processing command failed: " + e.getMessage())));
        }
    }

    /**
     * Handles the REGISTER command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleRegisterCommand(String jsonMessage) {
        RegisterRequest req = gson.fromJson(jsonMessage, RegisterRequest.class);
        try {
            Player registeredPlayer = authService.register(req.username, req.password);
            try {
                Player loggedInPlayer = authService.login(req.username, req.password);
                setAuthenticatedPlayer(loggedInPlayer);
                sendMessage(gson.toJson(new LoginResponse(true, "Login successful after registration.", loggedInPlayer.getIdAsString(), loggedInPlayer.getUsername())));
            } catch (AuthenticationException ae) {
                sendMessage(gson.toJson(new RegisterResponse(true, "Registration successful, but login failed: " + ae.getMessage(), registeredPlayer.getIdAsString())));
            }
        } catch (RegistrationException e) {
            sendMessage(gson.toJson(new RegisterResponse(false, e.getMessage(), null)));
        }
    }

    /**
     * Handles the LOGIN command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleLoginCommand(String jsonMessage) {
        LoginRequest req = gson.fromJson(jsonMessage, LoginRequest.class);
        try {
            Player loggedInPlayer = authService.login(req.username, req.password);
            setAuthenticatedPlayer(loggedInPlayer);
            sendMessage(gson.toJson(new LoginResponse(true, "Login successful.", loggedInPlayer.getIdAsString(), loggedInPlayer.getUsername())));

            if (networkManager != null && loggedInPlayer != null) {
                networkManager.ensureSingleSessionForPlayer(loggedInPlayer.getId(), this.sessionId);


                networkManager.updateShip(
                    loggedInPlayer.getId(),
                    loggedInPlayer.getLastSpaceX(),
                    loggedInPlayer.getLastSpaceY(),
                    loggedInPlayer.getLastSpaceAngle(),
                    0,
                    0,
                    false,
                    true,
                    this
                );
                networkManager.sendActiveShipsToSession(this);
                System.out.println("Session " + sessionId + ": Player " + loggedInPlayer.getId() + " logged in. Ship restored/created in space at X: " + loggedInPlayer.getLastSpaceX() + ", Y: " + loggedInPlayer.getLastSpaceY());
            }

        } catch (AuthenticationException e) {
            sendMessage(gson.toJson(new LoginResponse(false, e.getMessage(), null, null)));
        }
    }

    /**
     * Handles the LIST_GAMES command from the client.
     */
    private void handleListGamesCommand() {
        if (lobbyService == null) {
            sendMessage(gson.toJson(new ErrorMessage("GameLobbyService not available.")));
            return;
        }
        List<Game> games = lobbyService.getAvailableGames();
        List<GameInfo> gameInfos = games.stream()
                                        .map(game -> new GameInfo(String.valueOf(game.getGameId()), game.getPlanetName(), game.getCurrentPlayerCount()))
                                        .collect(Collectors.toList());
        sendMessage(gson.toJson(new ListGamesResponse(gameInfos)));
    }

    /**
     * Handles the JOIN_GAME command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleJoinGameCommand(String jsonMessage) {
        JoinGameRequest req = gson.fromJson(jsonMessage, JoinGameRequest.class);
        if (lobbyService == null) {
            sendMessage(gson.toJson(new ErrorMessage("GameLobbyService not available.")));
            return;
        }
        try {
            int gameId = Integer.parseInt(req.gameId);

            if (isAuthenticated() && networkManager != null && player != null) {
                PlayerShip currentPlayerShip = networkManager.getPlayerShip(player.getId());
                    if (currentPlayerShip != null) {
                        player.setLastSpaceX(currentPlayerShip.getX());
                        player.setLastSpaceY(currentPlayerShip.getY());
                        player.setLastSpaceAngle(currentPlayerShip.getOrientation());
                        player.save(); 
                        System.out.println("Session " + sessionId + ": Stored and saved last space location for player " + player.getId() + " - X: " + currentPlayerShip.getX() + ", Y: " + currentPlayerShip.getY() + ", Angle: " + currentPlayerShip.getOrientation());
                        networkManager.setShipLanded(player.getId(), this); 
                    } else {
    
                         System.out.println("Session " + sessionId + ": No active ship found for player " + player.getId() + " when joining game. Last known space coords will be used on exit, or defaults.");
                    }
                }
                GameManager joinedGame = lobbyService.joinGame(this, gameId);
                this.currentGameService = joinedGame;
                
                List<PlayerInitialData> playersInGameData = joinedGame.getPlayersInGame().stream()
                    .map(p -> new PlayerInitialData(p.getIdAsString(), p.getUsername(), p.getX(), p.getY(), p.getDx(), p.getDy(), p.getDirectionAngle()))
                    .collect(Collectors.toList());

                sendMessage(gson.toJson(new JoinGameResponse(true, "Successfully joined game.", String.valueOf(joinedGame.getGameId()), joinedGame.getPlanetName(), playersInGameData)));

            } catch (NumberFormatException ex) {
                sendMessage(gson.toJson(new JoinGameResponse(false, "Invalid game ID format", req.gameId, null, null)));
            } catch (GameJoinException e) {
                sendMessage(gson.toJson(new JoinGameResponse(false, e.getMessage(), req.gameId, null, null)));
            }
    }
    
    /**
     * Handles the LEAVE_GAME command from the client.
     */
    private void handleLeaveGameCommand() {
        if (lobbyService == null) {
            sendMessage(gson.toJson(new ErrorMessage("GameLobbyService not available.")));
            return;
        }
        if (currentGameService == null) {
            sendMessage(gson.toJson(new ErrorMessage("You are not in a game.")));
            return;
        }
        String gameIdStr = String.valueOf(currentGameService.getGameId());
        lobbyService.leaveGame(this, currentGameService.getGameId());
        this.currentGameService = null;
        sendMessage(gson.toJson(new LeaveGameResponse(true, "Successfully left game.", gameIdStr)));

        if (isAuthenticated() && networkManager != null && player != null) {
            double launchX = player.getLastSpaceX();
            double launchY = player.getLastSpaceY();
            double launchAngle = player.getLastSpaceAngle();
            
            networkManager.setShipLaunched(player.getId(), launchX, launchY, launchAngle, this);
            System.out.println("Session " + sessionId + ": Player " + player.getId() + " launched ship to space at X: " + launchX + ", Y: " + launchY + ", Angle: " + launchAngle);
        }
    }

    /**
     * Handles the UPDATE_PLAYER command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleUpdatePlayerCommand(String jsonMessage) {
        PlayerUpdateRequest req = gson.fromJson(jsonMessage, PlayerUpdateRequest.class);
        if (currentGameService == null) {
            sendMessage(gson.toJson(new ErrorMessage("Not in a game. Cannot update player.")));
            return;
        }
        if (player == null || !String.valueOf(player.getId()).equals(req.playerId)) {
             sendMessage(gson.toJson(new ErrorMessage("Player ID mismatch or not authenticated.")));
            return;
        }
        currentGameService.handlePlayerUpdate(this, (int)req.x, (int)req.y, req.dx, req.dy, req.directionAngle);
    }

    /**
     * Handles the GET_PLAYERS command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleGetPlayersCommand(String jsonMessage) {
        GetPlayersRequest req = gson.fromJson(jsonMessage, GetPlayersRequest.class);
        if (currentGameService == null || !String.valueOf(currentGameService.getGameId()).equals(req.gameId)) {
            sendMessage(gson.toJson(new ErrorMessage("Not in the specified game or game service unavailable.")));
            return;
        }
        List<PlayerInitialData> playersInGameData = currentGameService.getPlayersInGame().stream()
            .map(p -> new PlayerInitialData(p.getIdAsString(), p.getUsername(), p.getX(), p.getY(), p.getDx(), p.getDy(), p.getDirectionAngle()))
            .collect(Collectors.toList());
        sendMessage(gson.toJson(new GetPlayersResponse(playersInGameData)));
    }

    /**
     * Handles the REQUEST_CHUNK command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleRequestChunkCommand(String jsonMessage) {
        RequestChunkRequest req = gson.fromJson(jsonMessage, RequestChunkRequest.class);
        if (currentGameService == null || !String.valueOf(currentGameService.getGameId()).equals(req.gameId)) {
            sendMessage(gson.toJson(new ErrorMessage("Not in the specified game or game service unavailable.")));
            return;
        }
        Chunk chunk = currentGameService.getChunkData(req.chunkX, req.chunkY);
        if (chunk != null) {
            List<TileData> tileDataList = chunk.getTilesList().stream()
                .map(tile -> new TileData(tile.getX(), tile.getY(), tile.getType(), tile.getColorType().name()))
                .collect(Collectors.toList());
            sendMessage(gson.toJson(new RequestChunkResponse(req.chunkX, req.chunkY, tileDataList)));
        } else {
            sendMessage(gson.toJson(new ErrorMessage("Chunk not found or could not be generated.")));
        }
    }

    /**
     * Handles the REQUEST_PALETTE command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleRequestPaletteCommand(String jsonMessage) {
        RequestPaletteRequest req = gson.fromJson(jsonMessage, RequestPaletteRequest.class);
        if (currentGameService == null || !String.valueOf(currentGameService.getGameId()).equals(req.gameId)) {
            sendMessage(gson.toJson(new ErrorMessage("Not in the specified game or game service unavailable.")));
            return;
        }
        ColorPallete palette = currentGameService.getPlanetPalette();
        if (palette != null) {
            RequestPaletteResponse resp = new RequestPaletteResponse(
                palette.getPrimarySurface().getRed() + "," + palette.getPrimarySurface().getGreen() + "," + palette.getPrimarySurface().getBlue(),
                palette.getPrimaryLiquid().getRed() + "," + palette.getPrimaryLiquid().getGreen() + "," + palette.getPrimaryLiquid().getBlue(),
                palette.getSecondarySurface().getRed() + "," + palette.getSecondarySurface().getGreen() + "," + palette.getSecondarySurface().getBlue(),
                palette.getTertiarySurface().getRed() + "," + palette.getTertiarySurface().getGreen() + "," + palette.getTertiarySurface().getBlue(),
                palette.getHueShift().getRed() + "," + palette.getHueShift().getGreen() + "," + palette.getHueShift().getBlue(),
                palette.getRock().getRed() + "," + palette.getRock().getGreen() + "," + palette.getRock().getBlue()
            );
            sendMessage(gson.toJson(resp));
        } else {
            sendMessage(gson.toJson(new ErrorMessage("Palette not found for this game.")));
        }
    }

    /**
     * Sends a message to the client.
     * @param responseObject The object to serialize to JSON and send.
     */
    public void sendMessage(Object responseObject) {
        String jsonToSend = gson.toJson(responseObject); // Serialize once
        if (responseObject instanceof PlayerJoinedBroadcast) {
            PlayerJoinedBroadcast pjb = (PlayerJoinedBroadcast) responseObject;
            System.out.println("Session " + sessionId + ": Attempting to send PlayerJoinedBroadcast for player ID " + pjb.playerId + " (" + pjb.username + "). PrintWriter error state: " + (out == null ? "null" : out.checkError()));
        }
        
        if (out != null && !out.checkError()) {
            out.println(jsonToSend);
            if (responseObject instanceof PlayerJoinedBroadcast) {
                 System.out.println("Session " + sessionId + ": Successfully sent PlayerJoinedBroadcast for player ID " + ((PlayerJoinedBroadcast)responseObject).playerId);
            }
        } else {
            System.err.println("Session " + sessionId + ": PrintWriter not available or in error state. Cannot send: " + jsonToSend);
        }
    }

    /**
     * Sends a raw string message to the client.
     * @param message The raw string message to send.
     */
    public void sendRawMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
        } else {
            System.err.println("Session " + sessionId + ": PrintWriter not available or in error state. Cannot send raw message: " + message);
        }
    }

    /**
     * Closes the client session and releases resources.
     * @param reason The reason for closing the session.
     */
    public synchronized void close(String reason) {
        if (!running) return;
        running = false;

        System.out.println("Closing client session " + sessionId + " for " + 
                           (clientSocket != null && clientSocket.getInetAddress() != null ? clientSocket.getInetAddress().getHostAddress() : "unknown host") + 
                           ". Reason: " + reason);

        if (this.sessionListener != null) {
            try {
                this.sessionListener.onSessionClosed(this);
            } catch (Exception e) {
                System.err.println("Session " + sessionId + ": Error during onSessionClosed notification: " + e.getMessage());
            }
        }
        
        if (currentGameService != null && player != null && lobbyService != null) {
            try {
                lobbyService.leaveGame(this, currentGameService.getGameId());
            } catch (Exception e) {
                System.err.println("Session " + sessionId + ": Error leaving game " + 
                                   (currentGameService != null ? currentGameService.getGameId() : "N/A") + 
                                   " for player " + (player != null ? player.getUsername() : "N/A") + ": " + e.getMessage());
            } finally {
                currentGameService = null; 
            }
        }

        if (this.player != null) {
            Player playerToLogout = this.player; 

            if (this.currentGameService == null && networkManager != null && playerToLogout != null) {
                PlayerShip currentShip = networkManager.getPlayerShip(playerToLogout.getId());
                if (currentShip != null) { 
                    playerToLogout.setLastSpaceX(currentShip.getX());
                    playerToLogout.setLastSpaceY(currentShip.getY());
                    playerToLogout.setLastSpaceAngle(currentShip.getOrientation());
                    playerToLogout.save(); 
                }
            }
            
            this.player = null; 

            if (authService != null && playerToLogout != null) {
                try {
                    authService.logout(playerToLogout); 
                } catch (Exception e) {
                     System.err.println("Session " + sessionId + ": Error during authService.logout for player " + 
                                       (playerToLogout.getUsername()) + ": " + e.getMessage());
                }
            }
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("Session " + sessionId + ": Error closing input stream: " + e.getMessage());
        }
        
        if (out != null) {
            out.close();
        }
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (Exception e) {
            System.err.println("Session " + sessionId + ": Error closing client socket: " + e.getMessage());
        }
    }

    /**
     * Returns the currently authenticated player for this session.
     * @return The authenticated Player object, or null if not authenticated.
     */
    public Player getAuthenticatedPlayer() {
        return player;
    }

    /**
     * Sets the authenticated player for this session.
     * @param player The Player object to set as authenticated.
     */
    public void setAuthenticatedPlayer(Player player) {
        this.player = player;
    }

    /**
     * Checks if the session is authenticated.
     * @return true if the session has an authenticated player, false otherwise.
     */
    public boolean isAuthenticated() {
        return player != null;
    }

    /**
     * Returns the unique session ID for this client session.
     * @return The session ID as a String.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session listener for this client session.
     * @param listener The ClientSessionListener to set.
     */
    public void setSessionListener(ClientSessionListener listener) {
        this.sessionListener = listener;
    }

    public int getPlayerId() {
        return (this.player != null) ? this.player.getId() : 0;
    }

    /**
     * Handles the REQUEST_PLANETS_AREA command from the client.
     * @param jsonMessage The full message containing the command and parameters.
     */
    private void handleRequestPlanetsAreaCommand(String jsonMessage) { 
        RequestPlanetsAreaRequest req = gson.fromJson(jsonMessage, RequestPlanetsAreaRequest.class);
        try {
            if (lobbyService != null) {
                List<PlanetInfo> planetInfos = lobbyService.getPlanetsInArea(req.centerX, req.centerY, req.radius)
                    .stream()
                    .map(p -> {
                        ColorPallete cp = p.getColorPallete();
                        int hueShiftColorInt = (cp != null && cp.getHueShift() != null) ? cp.getHueShift().getRGB() : java.awt.Color.GRAY.getRGB(); 
                        return new PlanetInfo(String.valueOf(p.getPlanetId()), p.getName(), p.getGalaxyX(), p.getGalaxyY(), p.getWidth(), p.getType().name(), hueShiftColorInt);
                    })
                    .collect(Collectors.toList());
                sendMessage(gson.toJson(new RequestPlanetsAreaResponse(planetInfos)));
            } else {
                sendMessage(gson.toJson(new ErrorMessage("Service not available to fetch planet data.")));
            }
        } catch (Exception e) {
            System.err.println("Error handling REQUEST_PLANETS_AREA: " + e.getMessage());
            e.printStackTrace();
            sendMessage(gson.toJson(new ErrorMessage("Failed to retrieve planet data: " + e.getMessage())));
        }
    }

    private void handleShipUpdateCommand(String jsonMessage) {
        ShipUpdateRequest req = gson.fromJson(jsonMessage, ShipUpdateRequest.class);
        if (!isAuthenticated() || networkManager == null) {
            sendMessage(gson.toJson(new ErrorMessage("Not authenticated or NetworkManager not available for SHIP_UPDATE.")));
            return;
        }
        if (player == null || !String.valueOf(player.getId()).equals(req.playerId)) {
            sendMessage(gson.toJson(new ErrorMessage("SHIP_UPDATE player ID mismatch.")));
            return;
        }

   
        if (this.currentGameService != null) {
            return; 
        }

        networkManager.updateShip(getPlayerId(), req.x, req.y, req.angle, req.dx, req.dy, req.thrusting, true, this);
    }

    private void handleAttackRequest(String jsonMessage) {
        AttackRequest req = gson.fromJson(jsonMessage, AttackRequest.class);
        if (!isAuthenticated() || player == null) {
            sendMessage(gson.toJson(new ErrorMessage("Not authenticated.")));
            return;
        }
        if (currentGameService == null) {
            sendMessage(gson.toJson(new ErrorMessage("Not in a game.")));
            return;
        }
        currentGameService.handleAttackRequest(player, new com.tavuc.utils.Vector2D(req.directionX, req.directionY));
    }

    private void handleFireRequest(String jsonMessage) {
        if (!isAuthenticated() || player == null) {
            sendMessage(gson.toJson(new ErrorMessage("Not authenticated or player data missing.")));
            return;
        }

        if (networkManager == null) {
            sendMessage(gson.toJson(new ErrorMessage("Network service unavailable to process fire request.")));
            return;
        }

        FireRequest req = gson.fromJson(jsonMessage, FireRequest.class);

        CombatManager combatManager = networkManager.getCombatManager();
        if (combatManager == null) {
            System.out.println("Session " + sessionId + ": Player " + player.getId() + " tried to fire but CombatManager is null.");
            return;
        }

        PlayerShip playerShip = networkManager.getPlayerShip(player.getId());
        boolean fireSuccessful = false;

        if (playerShip != null) {
            fireSuccessful = combatManager.processFireRequest(String.valueOf(player.getId()), this);
        } else if (req != null) {
            fireSuccessful = combatManager.processFireRequest(
                String.valueOf(player.getId()),
                req.shipX,
                req.shipY,
                req.shipAngle,
                req.shipDx,
                req.shipDy
            );
        } else {
            System.out.println("Session " + sessionId + ": FireRequest data missing and no active ship instance.");
        }

        if (!fireSuccessful) {
            // Optional: send cooldown or error message
        }
    }
}
