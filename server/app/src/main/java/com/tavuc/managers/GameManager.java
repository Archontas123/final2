package com.tavuc.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import com.tavuc.networking.models.ProjectileSpawnedBroadcast;
import com.tavuc.networking.models.ProjectileUpdateBroadcast;
import com.tavuc.networking.models.ProjectileRemovedBroadcast;
import com.tavuc.models.space.BaseShip;   // Added import
import com.tavuc.models.space.ProjectileEntity;


public class GameManager {

    private int gameId;
    private Planet planet;
    private String planetName;
    private int maxPlayers;
    private final Map<Player, ClientSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, Player> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, BaseShip> aiShips = new ConcurrentHashMap<>(); // Implemented AI ship tracking
    // Track last melee attack times for cooldowns
    private final ConcurrentMap<Integer, Long> lastMeleeAttackTimes = new ConcurrentHashMap<>();

    // Damage dealt by players while on the ground
    private static final double PLAYER_ATTACK_DAMAGE = 0.5;
    // Starting health for players when on the ground - changed to match client expectations
    private static final double PLAYER_START_HEALTH = 6.0; // Changed from 3.0 to 6.0
    // Cooldown between melee attacks (ms)
    private static final long PLAYER_ATTACK_COOLDOWN_MS = 300;

    // --- Gun mechanics ---
    private static final float BULLET_SPEED = 120.0f;
    private static final float BULLET_DAMAGE = 0.5f;
    private static final int BULLET_WIDTH = 6;
    private static final int BULLET_HEIGHT = 6;
    private static final long PLAYER_SHOOT_COOLDOWN_MS = 300;

    private final Map<String, ProjectileEntity> groundProjectiles = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Long> lastShootTimes = new ConcurrentHashMap<>();

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

        // Ensure the player starts the match with full health (6 half-hearts = 3 full hearts)
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


        System.out.println("GameService " + gameId + ": Player " + player.getUsername() + " (ID: " + player.getId() + ") with session " + session.getSessionId() + " added to game with " + PLAYER_START_HEALTH + " health.");
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

        // Clean up cooldown tracking for the player
        lastMeleeAttackTimes.remove(player.getId());

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

        long now = System.currentTimeMillis();
        Long lastTime = lastMeleeAttackTimes.get(attackerId);
        if (lastTime != null && now - lastTime < PLAYER_ATTACK_COOLDOWN_MS) {
            System.out.println("GameService " + gameId + ": Attack from " + attackerId + " on cooldown");
            return;
        }
        lastMeleeAttackTimes.put(attackerId, now);

        double dx = target.getX() - attacker.getX();
        double dy = target.getY() - attacker.getY();
        double range = attacker.getAttackRange();

        double distance = Math.hypot(dx, dy);
        System.out.println("GameService " + gameId + ": Attack attempt " + attackerId + " -> " + targetId + " at " + System.currentTimeMillis() + " distance=" + distance + " range=" + range);
        if (distance > range) {
            System.out.println("GameService " + gameId + ": Attack out of range");
            return;
        }
        double angle = Math.atan2(dy, dx);
        int dashX = (int)(target.getX() - Math.cos(angle) * 10);
        int dashY = (int)(target.getY() - Math.sin(angle) * 10);
        attacker.setPosition(dashX, dashY);
        PlayerMovedBroadcast dashMsg = new PlayerMovedBroadcast(
                attacker.getIdAsString(), dashX, dashY, attacker.getDx(), attacker.getDy(), attacker.getDirectionAngle());
        broadcastToGame(dashMsg);
        // Log the attack for debugging
        System.out.println("GameService " + gameId + ": Player " + attackerId + " attacks " + targetId +
                          " for " + PLAYER_ATTACK_DAMAGE + " damage. Target health before: " + target.getHealth());

        target.takeDamage(PLAYER_ATTACK_DAMAGE);

        System.out.println("GameService " + gameId + ": Target health after damage: " + target.getHealth());

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

    public synchronized void handlePlayerShoot(int shooterId, double x, double y, double directionAngle) {
        Player shooter = null;
        for (Player p : playerSessions.keySet()) {
            if (p.getId() == shooterId) {
                shooter = p;
                break;
            }
        }
        if (shooter == null) return;

        long now = System.currentTimeMillis();
        Long last = lastShootTimes.get(shooterId);
        if (last != null && now - last < PLAYER_SHOOT_COOLDOWN_MS) {
            return;
        }
        lastShootTimes.put(shooterId, now);

        // Ignore client-provided spawn coordinates and compute from shooter state
        float orientation = (float) shooter.getDirectionAngle();
        float spawnX = shooter.getX() + shooter.getWidth() / 2f +
                       (float) Math.cos(orientation) * shooter.getWidth() / 2f;
        float spawnY = shooter.getY() + shooter.getHeight() / 2f +
                       (float) Math.sin(orientation) * shooter.getHeight() / 2f;
        float vx = (float) (Math.cos(orientation) * BULLET_SPEED);
        float vy = (float) (Math.sin(orientation) * BULLET_SPEED);

        String projId = "gproj_" + java.util.UUID.randomUUID();
        ProjectileEntity proj = new ProjectileEntity(
            projId, spawnX, spawnY, BULLET_WIDTH, BULLET_HEIGHT,
            orientation, vx, vy, BULLET_DAMAGE, String.valueOf(shooterId));
        groundProjectiles.put(projId, proj);

        ProjectileSpawnedBroadcast spawnMsg = new ProjectileSpawnedBroadcast(
            projId, spawnX, spawnY, BULLET_WIDTH, BULLET_HEIGHT,
            orientation, BULLET_SPEED, vx, vy, BULLET_DAMAGE,
            String.valueOf(shooterId));
        broadcastToGame(spawnMsg);
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
            Map<Player, int[]> prevPositions = new HashMap<>();

            for (Player player : players) {
                int prevX = player.getX();
                int prevY = player.getY();
                prevPositions.put(player, new int[] { prevX, prevY });

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
                        if (box.intersects(t.getHitBox())) {
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

            // Resolve collisions between players
            for (int i = 0; i < players.size(); i++) {
                Player a = players.get(i);
                for (int j = i + 1; j < players.size(); j++) {
                    Player b = players.get(j);
                    if (a.getHurtbox().intersects(b.getHurtbox())) {
                        int[] posA = prevPositions.get(a);
                        int[] posB = prevPositions.get(b);
                        if (posA != null) {
                            a.setPosition(posA[0], posA[1]);
                            a.setDx(0);
                            a.setDy(0);
                            Rectangle boxA = a.getHurtbox();
                            boxA.setLocation(
                                posA[0] + (a.getWidth() - boxA.width) / 2,
                                posA[1] + (a.getHeight() - boxA.height) / 2
                            );
                        }
                        if (posB != null) {
                            b.setPosition(posB[0], posB[1]);
                            b.setDx(0);
                            b.setDy(0);
                            Rectangle boxB = b.getHurtbox();
                            boxB.setLocation(
                                posB[0] + (b.getWidth() - boxB.width) / 2,
                                posB[1] + (b.getHeight() - boxB.height) / 2
                            );
                        }
                    }
                }
            }

            // Iterate through and update AI ships
            for (BaseShip aiShip : aiShips.values()) {
               aiShip.update();
            }

            // --- Update ground projectiles ---
            List<String> toRemove = new ArrayList<>();
            for (ProjectileEntity proj : groundProjectiles.values()) {
                proj.update(1f/60f);

                boolean removed = false;
                for (Player target : players) {
                    if (String.valueOf(target.getId()).equals(proj.getOwnerId())) continue;
                    float dist = (float)Math.hypot(target.getX()+target.getWidth()/2 - proj.getX(),
                                                 target.getY()+target.getHeight()/2 - proj.getY());
                    if (dist < 30) {
                        target.takeDamage(BULLET_DAMAGE);
                        PlayerDamagedBroadcast dmg = new PlayerDamagedBroadcast(
                            target.getIdAsString(), BULLET_DAMAGE, target.getHealth());
                        broadcastToGame(dmg);
                        if (target.getHealth() <= 0) {
                            PlayerKilledBroadcast killed = new PlayerKilledBroadcast(target.getIdAsString(), proj.getOwnerId());
                            broadcastToGame(killed);
                            removePlayer(target, playerSessions.get(target));
                        }
                        removed = true;
                        break;
                    }
                }

                if (proj.getLifetime() > 5f) {
                    removed = true;
                }

                if (removed) {
                    toRemove.add(proj.getId());
                    ProjectileRemovedBroadcast rm = new ProjectileRemovedBroadcast(proj.getId());
                    broadcastToGame(rm);
                } else {
                    ProjectileUpdateBroadcast up = new ProjectileUpdateBroadcast(proj.getId(), proj.getX(), proj.getY(), proj.getVelocityX(), proj.getVelocityY());
                    broadcastToGame(up);
                }
            }
            for (String id : toRemove) {
                groundProjectiles.remove(id);
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
}
