package com.tavuc.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.tavuc.models.GameObject;
import com.tavuc.models.entities.Dummy;
import com.tavuc.models.entities.Entity;
import com.tavuc.models.entities.Player;
import com.tavuc.models.planets.Chunk;
import com.tavuc.models.planets.ColorPallete;
import com.tavuc.models.planets.Planet;
import com.tavuc.models.planets.Tile;
import com.tavuc.networking.ClientSession;
import com.tavuc.networking.models.DummyUpdateBroadcast;
import com.tavuc.networking.models.DummyRemovedBroadcast;
import com.tavuc.networking.models.PlayerJoinedBroadcast;
import com.tavuc.networking.models.PlayerLeftBroadcast;
import com.tavuc.networking.models.PlayerMovedBroadcast;
import com.tavuc.models.space.BaseShip;   // Added import
import com.tavuc.networking.models.AttackResultBroadcast;
import com.tavuc.networking.models.AttackResultData;
import com.tavuc.models.combat.HitDetectionSystem;
import com.tavuc.utils.Vector2D;
import com.tavuc.models.combat.PlayerCombatComponent;


public class GameManager {

    private int gameId;
    private Planet planet;
    private String planetName;
    private int maxPlayers;
    private final Map<Player, ClientSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, BaseShip> aiShips = new ConcurrentHashMap<>(); // Implemented AI ship tracking
    private final Map<Integer, Dummy> dummies = new ConcurrentHashMap<>();
    private int nextDummyId = 0;
    private HitDetectionSystem hitDetection = new HitDetectionSystem();

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
        // Spawn some Dummies
        spawnDummies(5); // Example: Spawn 5 dummies
        System.out.println("GameService " + gameId + " (" + planetName + ") initialized with max " + maxPlayers + " players.");
    }

    private void spawnDummies(int count) {
        if (playerSessions.isEmpty()) {
            System.out.println("GameManager " + gameId + ": No players in game, not spawning dummies.");
            return;
        }

        List<Player> currentPlayers = new ArrayList<>(playerSessions.keySet());
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < count; i++) {
            Player targetPlayer = currentPlayers.get(random.nextInt(currentPlayers.size()));
            
            float spawnRadius = 100.0f; // Max distance from player
            float angle = (float) (random.nextDouble() * 2 * Math.PI);
            float distance = (float) (random.nextDouble() * spawnRadius);
            
            float x = targetPlayer.getX() + (float) (Math.cos(angle) * distance);
            float y = targetPlayer.getY() + (float) (Math.sin(angle) * distance);

            // Ensure dummies are within planet bounds if applicable - for now, simple offset
            // This might need adjustment based on how planet boundaries are defined and checked.
            // Example: Clamp to a generic 0-500 range if no specific planet bounds logic is available here.
            // x = Math.max(0, Math.min(x, 500)); 
            // y = Math.max(0, Math.min(y, 500));

            Dummy dummy = new Dummy(nextDummyId++, x, y);
            dummies.put(dummy.getId(), dummy);
            System.out.println("GameManager " + gameId + ": Spawned Dummy " + dummy.getId() + " at (" + x + ", " + y + ") near Player " + targetPlayer.getId());
        }
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

        // If this is the first player, try spawning dummies
        if (playerSessions.size() == 1) {
            spawnDummies(5); // Or a configurable number
        }

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

        // Send existing players' info to the new player
        for (Map.Entry<Player, ClientSession> entry : playerSessions.entrySet()) {
            Player existingPlayer = entry.getKey();
            // Don't send the new player's own info back to them
            if (!existingPlayer.getIdAsString().equals(player.getIdAsString())) {
                PlayerJoinedBroadcast existingPlayerMsg = new PlayerJoinedBroadcast(
                        existingPlayer.getIdAsString(),
                        existingPlayer.getUsername(),
                        existingPlayer.getX(),
                        existingPlayer.getY(),
                        existingPlayer.getDx(),
                        existingPlayer.getDy(),
                        existingPlayer.getDirectionAngle()
                );
                System.out.println("GameService " + gameId + ": Sending existing player " + existingPlayer.getUsername() + " (ID: " + existingPlayer.getIdAsString() + ") info to new player " + player.getUsername() + " (ID: " + player.getIdAsString() + ")");
                session.sendMessage(existingPlayerMsg);
            }
        }

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

        playerToUpdate.setDx(dx);
        playerToUpdate.setDy(dy);
        playerToUpdate.setDirectionAngle(directionAngle);
    }

    public void handleAttackRequest(Player player, Vector2D direction) {
        if (player == null) return;
        PlayerCombatComponent combat = player.getCombatComponent();
        if (combat != null) {
            combat.attemptAttack(direction.normalize());
        }
    }

    public void handleParryRequest(Player player) {
        if (player == null) return;
        PlayerCombatComponent combat = player.getCombatComponent();
        if (combat != null) {
            combat.attemptParry();
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
                player.update(); 
            }

            // Iterate through and update AI ships
            for (BaseShip aiShip : aiShips.values()) {
               aiShip.update();
            }

            // Update Dummies
            for (Dummy dummy : dummies.values()) {
                dummy.update();
                DummyUpdateBroadcast dummyUpdateMsg = new DummyUpdateBroadcast(dummy.getId(), dummy.getX(), dummy.getY(), dummy.getDx(), dummy.getDy());
                broadcastToGame(dummyUpdateMsg);
            }

            updatePlayerCombat();
            processAttacks();

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

    private void updatePlayerCombat() {
        for (Player player : getPlayersInGame()) {
            if (player.getCombatComponent() != null) {
                player.getCombatComponent().update();
            }
        }
    }

    private static final double ATTACK_CONE_ANGLE = Math.PI / 2.0; // 90 degree cone

    private void processAttacks() {
        for (Player attacker : getPlayersInGame()) {
            PlayerCombatComponent combat = attacker.getCombatComponent();
            if (combat == null || !combat.isAttacking()) continue;
            if (combat.wasAttackProcessed()) continue;

            Vector2D dir = combat.getAttackDirection();
            if (dir == null) continue;

            java.util.List<Player> hitPlayers = hitDetection.detectMeleeHits(attacker, dir, this);
            float damage = combat.getEquippedWeapon() != null ? combat.getEquippedWeapon().getDamage() : 10.0f;

            java.util.List<AttackResultData> results = new java.util.ArrayList<>();
            for (Player target : hitPlayers) {
                if (!isWithinAttackCone(attacker, target, ATTACK_CONE_ANGLE)) {
                    continue;
                }

                PlayerCombatComponent tc = target.getCombatComponent();
                if (tc != null) {
                    if (tc.isParrying()) {
                        results.add(new AttackResultData(target.getIdAsString(), 0, tc.getHealth(), true));
                    } else {
                        tc.takeDamage(damage, attacker);
                        results.add(new AttackResultData(target.getIdAsString(), damage, tc.getHealth(), false));
                    }
                }
            }

            combat.setAttackProcessed(true);

            if (!results.isEmpty()) {
                AttackResultBroadcast br = new AttackResultBroadcast(attacker.getIdAsString(), dir.x, dir.y, results);
                broadcastToGame(br);
            }
        }
    }

    /**
     * Determines if the target player lies within the attack cone in front of the attacker.
     *
     * @param attacker Attacking player
     * @param target Target player
     * @param coneAngle Total width of the cone in radians
     * @return True if the target is inside the cone
     */
    private boolean isWithinAttackCone(Player attacker, Player target, double coneAngle) {
        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        double angleToTarget = Math.atan2(dy, dx);
        double attackerAngle = attacker.getDirectionAngle();
        double diff = normalizeAngle(angleToTarget - attackerAngle);
        return Math.abs(diff) <= coneAngle / 2.0;
    }

    /**
     * Normalizes an angle to the range [-PI, PI].
     */
    private double normalizeAngle(double a) {
        while (a <= -Math.PI) a += 2.0 * Math.PI;
        while (a > Math.PI) a -= 2.0 * Math.PI;
        return a;
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
