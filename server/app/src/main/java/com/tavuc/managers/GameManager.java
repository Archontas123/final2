package com.tavuc.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.tavuc.models.GameObject;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.Planet;
import com.tavuc.models.planets.Tile;
import com.tavuc.networking.ClientSession;
import com.tavuc.models.space.AttackShip; // Added import
import com.tavuc.models.space.BaseShip;   // Added import
import com.tavuc.models.space.LightCruiser; // Added import for findLightCruiserById

public class GameManager {

    private int gameId;
    private Planet planet;
    private String planetName;
    private int maxPlayers;
    private final Map<Player, ClientSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, BaseShip> aiShips = new ConcurrentHashMap<>(); // Implemented AI ship tracking

    /**
     * Initializes the GameService with a game ID, planet, and maximum number of players.
     * @param gameId Unique identifier for the game.
     * @param planet The planet associated with this game.
     * @param maxPlayers Maximum number of players allowed in the game.
     */
    public void initialize(int gameId, Planet planet, int maxPlayers) {
        this.gameId = gameId;
        this.planet = planet;
        this.planetName = planet.getName();
        this.maxPlayers = maxPlayers;     
        System.out.println("GameService " + gameId + " (" + planetName + ") initialized with max " + maxPlayers + " players.");
    }

    /**
     * Adds a player to the game with the associated session.
     * @param player The player to be added.
     * @param session The session associated with the player.
     * @return true if the player was successfully added, false otherwise.
     */
    public synchronized boolean addPlayer(Player player, ClientSession session) {
        if (player == null || session == null) {
            System.err.println("GameService " + gameId + ": Attempted to add null player or session.");
            return false;
        }
        if (playerSessions.size() >= maxPlayers) {
            System.out.println("GameService " + gameId + ": Game is full. Cannot add player " + player.getUsername());
            session.sendMessage("ERROR Game is full.");
            return false;
        }
        if (playerSessions.containsKey(player) || sessionToPlayer.containsKey(session.getSessionId())) {
            System.out.println("GameService " + gameId + ": Player " + player.getUsername() + " (ID: " + player.getId() + ") or session " + session.getSessionId() + " already in this game. Not re-adding.");
            return false;
        }

        playerSessions.put(player, session);
        sessionToPlayer.put(session.getSessionId(), player);

        // Update AI ship targets if a player with the same ID re-joins or is updated
        for (BaseShip ship : aiShips.values()) {
            if (ship instanceof LightCruiser) {
                LightCruiser lc = (LightCruiser) ship;
                Player oldTarget = lc.getTargetPlayer();
                if (oldTarget != null && oldTarget.getId() == player.getId() && oldTarget != player) {
                    lc.setTargetPlayer(player);
                    System.out.println("GameService " + gameId + ": Updated LightCruiser " + lc.getEntityId() + " target to new Player instance for ID " + player.getId());
                }
            }
            if (ship instanceof AttackShip) {
                AttackShip as = (AttackShip) ship;
                Player oldTarget = as.getTargetPlayerObject();
                if (oldTarget != null && oldTarget.getId() == player.getId() && oldTarget != player) {
                    as.setTargetPlayerObject(player);
                    System.out.println("GameService " + gameId + ": Updated AttackShip " + as.getEntityId() + " target to new Player instance for ID " + player.getId());
                }
            }
        }
        
        String newPlayerJoinedMsg = "PLAYER_JOINED " + player.getId() + " " + player.getUsername() + " " + player.getX() + " " + player.getY() + " " + player.getDx() + " " + player.getDy() + " " + player.getDirectionAngle();
        broadcastToGameExceptSender(newPlayerJoinedMsg, session);

        System.out.println("GameService " + gameId + ": Player " + player.getUsername() + " (ID: " + player.getId() + ") with session " + session.getSessionId() + " added to game.");
        return true;
    }

    /**
     * Removes a player from the game and their associated session.
     * @param player The player to be removed.
     * @param session The session associated with the player, can be null if not available.
     */
    public synchronized void removePlayer(Player player, ClientSession session) {
        if (player == null) return;

        playerSessions.remove(player);
        if (session != null) {
            sessionToPlayer.remove(session.getSessionId());
        } else {
            ClientSession sessionToRemove = null;
            for(Map.Entry<Player, ClientSession> entry : playerSessions.entrySet()){
                if(entry.getKey().equals(player)){
                    sessionToRemove = entry.getValue();
                    break;
                }
            }
            if(sessionToRemove != null) sessionToPlayer.remove(sessionToRemove.getSessionId());
        }
        
        String playerLeftMsg = "PLAYER_LEFT " + player.getId();
        broadcastToGame(playerLeftMsg);

        System.out.println("GameService " + gameId + ": Player " + player.getUsername() + " (ID: " + player.getId() + ") removed from game.");
    }

    /**
     * Handles player updates such as movement and direction changes.
     * @param clientSession The session of the player sending the update.
     * @param x The new x-coordinate of the player.
     * @param y The new y-coordinate of the player.
     * @param dx The change in x-coordinate (velocity).
     * @param dy The change in y-coordinate (velocity).
     * @param directionAngle The new direction angle of the player.
     */
    public void handlePlayerUpdate(ClientSession clientSession, int x, int y, double dx, double dy, double directionAngle) {
        Player playerToUpdate = sessionToPlayer.get(clientSession.getSessionId());

        if (playerToUpdate == null) {
            System.err.println("GameService " + gameId + ": Received player update from unknown session: " + clientSession.getSessionId());
            clientSession.sendMessage("ERROR You are not recognized in this game.");
            return;
        }

        playerToUpdate.setDx(dx);
        playerToUpdate.setDy(dy);
        playerToUpdate.setDirectionAngle(directionAngle);
    }

    /**
     * Retrieves the chunk data for a specific chunk in the planet associated with this game.
     * @param chunkX The x-coordinate of the chunk.
     * @param chunkY The y-coordinate of the chunk.
     * @return The Chunk object containing the data for the specified chunk, or null if the planet is not set.
     */
    public Chunk getChunkData(int chunkX, int chunkY) {
        if (this.planet != null) {
            return this.planet.getChunk(chunkX, chunkY);
        }
        return null;
    }

    /**
     * Retrieves the color palette for the planet associated with this game.
     * @return The ColorPallete object for the planet, or null if the planet is not set.
     */
    public ColorPallete getPlanetPalette() {
        if (this.planet != null) {
            return this.planet.getColorPallete();
        }
        return null;
    }
    
    /**
     * Retrieves a list of players currently in the game.
     * @return A list of Player objects representing the players in the game.
     */
    public synchronized List<Player> getPlayersInGame() {
        return new ArrayList<>(playerSessions.keySet());
    }

    /**
     * Broadcasts a message to all players in the game.
     * @param message The message to be sent to all players.
     */
    public void broadcastToGame(String message) {
        for (ClientSession session : playerSessions.values()) {
            session.sendMessage(message);
        }
    }

    /**
     * Broadcasts a message to all players in the game except the sender.
     * @param message The message to be sent to all players except the sender.
     * @param sender The session of the player who sent the message, to be excluded from the broadcast.
     */
    public void broadcastToGameExceptSender(String message, ClientSession sender) {
        for (Map.Entry<Player, ClientSession> entry : playerSessions.entrySet()) {
            if (entry.getValue() != sender) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    /**
     * Updates the state of all players in the game.
     * Also, TODO: update AI entities.
     */
    public void update() {
        synchronized (playerSessions) {
            List<Player> players = new ArrayList<>(playerSessions.keySet());

            for (Player player : players) {
                player.update(); 
            }

            // Iterate through and update AI ships
            for (BaseShip aiShip : aiShips.values()) {
               aiShip.update();
            }


            for (Player player : players) {
                ClientSession session = playerSessions.get(player);
                String finalUpdateMsg = "PLAYER_MOVED " + player.getId() + " " +
                                       player.getX() + " " + player.getY() + " " +
                                       player.getDx() + " " + player.getDy() + " " +
                                       player.getDirectionAngle();
                if (session != null) {
                    broadcastToGameExceptSender(finalUpdateMsg, session);
                } else {
                    System.err.println("GameManager " + gameId + ": Session not found for player " + player.getId() + " (username: " + player.getUsername() + ") during final broadcast. Broadcasting to all as fallback.");
                    broadcastToGame(finalUpdateMsg); 
                }
            }
            // TODO: Broadcast updates for AI ships if necessary
        }
    }

    /**
     * Gets the unique identifier for this game.
     * @return The game ID.
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Gets the name of the planet associated with this game.
     * @return The name of the planet.
     */
    public String getPlanetName() {
        return planetName;
    }

    /**
     * Gets the planet associated with this game.
     * @return The Planet object.
     */
    public Planet getPlanet() {
        return planet;
    }

    /**
     * Gets the maximum number of players allowed in this game.
     * @return The maximum player count.
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    /**
     * Finds a LightCruiser by its entity ID within this game instance.
     * Placeholder: Actual implementation would require GameManager to track AI entities.
     * @param cruiserId The ID of the cruiser to find.
     * @return The LightCruiser object if found, otherwise null.
     */
    public LightCruiser findLightCruiserById(String cruiserId) {
        BaseShip ship = aiShips.get(cruiserId);
        if (ship instanceof LightCruiser) {
            return (LightCruiser) ship;
        }
        return null; 
    }

    /**
     * Spawns an AttackShip and adds it to the game.
     * @param x Initial x-coordinate.
     * @param y Initial y-coordinate.
     * @param width Width of the AttackShip.
     * @param height Height of the AttackShip.
     * @param targetPlayer The player this AttackShip will target.
     * @param parentCruiserId The ID of the LightCruiser that spawned this AttackShip.
     * @return The newly created AttackShip, or null if creation failed.
     */
    public AttackShip spawnAttackShip(int x, int y, int width, int height, Player targetPlayer, String parentCruiserId) {
        String newShipId = "AS_" + java.util.UUID.randomUUID().toString(); // AS for AttackShip
        AttackShip newAttackShip = new AttackShip(newShipId, x, y, width, height, targetPlayer, parentCruiserId);
        aiShips.put(newShipId, newAttackShip);
        System.out.println("GameManager " + gameId + ": Spawned AttackShip " + newShipId + " targeting Player " + targetPlayer.getIdAsString() + " from Cruiser " + parentCruiserId);
        return newAttackShip;
    }

    /**
     * Adds an existing AI ship (e.g. a pre-placed LightCruiser) to the game's tracking.
     * @param ship The AI ship to add.
     */
    public void addAiShip(BaseShip ship) {
        if (ship != null && !aiShips.containsKey(ship.getEntityId())) {
            aiShips.put(ship.getEntityId(), ship);
            System.out.println("GameManager " + gameId + ": Added AI Ship " + ship.getEntityId() + " to tracking.");
        }
    }
    
    /**
     * Removes an AI ship from the game's tracking.
     * @param shipId The ID of the AI ship to remove.
     */
    public void removeAiShip(String shipId) {
        if (aiShips.containsKey(shipId)) {
            aiShips.remove(shipId);
            System.out.println("GameManager " + gameId + ": Removed AI Ship " + shipId + " from tracking.");
            // TODO: Potentially notify clients about AI ship removal if necessary
        }
    }

    public Map<String, BaseShip> getAiShips() {
        return aiShips;
    }
}
