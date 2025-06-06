package com.tavuc.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import java.awt.Rectangle;

import com.tavuc.models.GameObject;
import com.tavuc.models.entities.Entity;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.Planet;
import com.tavuc.models.planets.Tile;
import com.tavuc.networking.ClientSession;
import com.tavuc.networking.models.PlayerJoinedBroadcast;
import com.tavuc.networking.models.PlayerLeftBroadcast;
import com.tavuc.networking.models.PlayerMovedBroadcast;
import com.tavuc.networking.models.PlayerDamagedBroadcast;
import com.tavuc.networking.models.PlayerKilledBroadcast;
import com.tavuc.models.space.BaseShip;   // Added import


public class GameManager {

    private int gameId;
    private Planet planet;
    private String planetName;
    private int maxPlayers;
    private final Map<Player, ClientSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, BaseShip> aiShips = new ConcurrentHashMap<>(); // Implemented AI ship tracking

    // Damage dealt by players while on the ground
    private static final double PLAYER_ATTACK_DAMAGE = 0.5;
    // Starting health for players when on the ground
    private static final double PLAYER_START_HEALTH = 3.0;
    // Allowed facing arc (in radians) for melee attacks
    private static final double PLAYER_ATTACK_ARC = Math.toRadians(45.0);

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

        // Ensure the player starts the match with full health
        player.setHealth(PLAYER_START_HEALTH);

        playerSessions.put(player, session);
        sessionToPlayer.put(session.getSessionId(), player);


        PlayerJoinedBroadcast newPlayerJoinedMsg = new PlayerJoinedBroadcast(
                player.getIdAsString(), 
                player.getUsername(), 
                player.getX(), 
                player.getY(), 
                player.getDx(), 
                player.getDy(), 
                player.getDirectionAngle()
        );
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
        
        PlayerLeftBroadcast playerLeftMsg = new PlayerLeftBroadcast(player.getIdAsString());
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

        // Update the player's absolute position based on the values supplied by
        // the client. Previously only the velocity (dx/dy) was recorded which
        // caused the server to integrate movement independently and quickly lead
        // to desynchronised positions between clients. By explicitly updating
        // the player's coordinates here we ensure the server state mirrors the
        // client's authoritative position for the current tick.
        playerToUpdate.setPosition(x, y);

        playerToUpdate.setDx(dx);
        playerToUpdate.setDy(dy);
        playerToUpdate.setDirectionAngle(directionAngle);
    }

    /**
     * Handles a player attacking another player on the ground.
     * @param attackerId The ID of the attacking player
     * @param targetId   The ID of the target player
     */
    public synchronized void handlePlayerAttack(int attackerId, int targetId) {
        Player attacker = null;
        Player target = null;
        for (Player p : playerSessions.keySet()) {
            if (p.getId() == attackerId) attacker = p;
            if (p.getId() == targetId) target = p;
        }
        if (attacker == null || target == null) return;

        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double range = attacker.getAttackRange();
        if (distance > range) {
            System.out.println("GameService " + gameId + ": Attack out of range (" + distance + "/" + range + ")");
            return;
        }

        double angleToTarget = Math.atan2(dy, dx);
        double angleDiff = angleDifference(angleToTarget, attacker.getDirectionAngle());
        if (angleDiff > PLAYER_ATTACK_ARC) {
            System.out.println("GameService " + gameId + ": Attack outside arc(" + Math.toDegrees(angleDiff) + "°)");
            return;
        }

        target.takeDamage(PLAYER_ATTACK_DAMAGE);

        PlayerDamagedBroadcast dmg = new PlayerDamagedBroadcast(
                target.getIdAsString(),
                PLAYER_ATTACK_DAMAGE,
                target.getHealth()
        );
        broadcastToGame(dmg);

        if (target.getHealth() <= 0) {
            PlayerKilledBroadcast killed = new PlayerKilledBroadcast(
                    target.getIdAsString(),
                    attacker.getIdAsString()
            );
            broadcastToGame(killed);
            // Remove the target from the game so other clients stop receiving
            // position updates and the player disappears from the planet.
            removePlayer(target, playerSessions.get(target));
        }
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
     * @param messageObject The message object to be sent to all players.
     */
    public void broadcastToGame(Object messageObject) {
        for (ClientSession session : playerSessions.values()) {
            session.sendMessage(messageObject);
        }
    }

    /**
     * Broadcasts a message to all players in the game except the sender.
     * @param messageObject The message object to be sent to all players except the sender.
     * @param sender The session of the player who sent the message, to be excluded from the broadcast.
     */
    public void broadcastToGameExceptSender(Object messageObject, ClientSession sender) {
        for (Map.Entry<Player, ClientSession> entry : playerSessions.entrySet()) {
            if (entry.getValue() != sender) {
                entry.getValue().sendMessage(messageObject);
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
                int prevX = player.getX();
                int prevY = player.getY();

                player.update();

                if (planet != null) {
                    Rectangle box = player.getHurtbox();
                    List<Tile> solids = planet.getNearbySolidTiles(
                            box.x,
                            box.y,
                            box.width,
                            box.height,
                            1
                    );
                    for (Tile t : solids) {
                        if (box.intersects(t.getHitbox())) {
                            player.setPosition(prevX, prevY);
                            player.setDx(0);
                            player.setDy(0);
                            box.setLocation(
                                prevX + (player.getWidth() - box.width) / 2,
                                prevY + (player.getHeight() - box.height) / 2
                            );
                            break;
                        }
                    }
                }
            }

            // Iterate through and update AI ships
            for (BaseShip aiShip : aiShips.values()) {
               aiShip.update();
            }



            for (Player player : players) {
                ClientSession session = playerSessions.get(player);
                PlayerMovedBroadcast playerMovedMsg = new PlayerMovedBroadcast(
                        player.getIdAsString(),
                        player.getX(),
                        player.getY(),
                        player.getDx(),
                        player.getDy(),
                        player.getDirectionAngle()
                );
                if (session != null) {
                    broadcastToGameExceptSender(playerMovedMsg, session);
                } else {
                    System.err.println("GameManager " + gameId + ": Session not found for player " + player.getId() + " (username: " + player.getUsername() + ") during final broadcast. Broadcasting to all as fallback.");
                    broadcastToGame(playerMovedMsg); 
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

    /**
     * Calculates the absolute smallest difference between two angles.
     * The result is always in the range [0, Math.PI].
     */
    private static double angleDifference(double a, double b) {
        double diff = a - b;
        diff = Math.atan2(Math.sin(diff), Math.cos(diff));
        return Math.abs(diff);
    }
}
