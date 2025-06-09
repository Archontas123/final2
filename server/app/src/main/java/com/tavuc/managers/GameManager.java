package com.tavuc.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.awt.Rectangle;
import java.time.Duration;

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
import com.tavuc.networking.models.CoinUpdateBroadcast;
import com.tavuc.networking.models.CoinDropSpawnedBroadcast;
import com.tavuc.networking.models.CoinDropRemovedBroadcast;
import com.tavuc.networking.models.EnemySpawnedBroadcast;
import com.tavuc.networking.models.EnemyUpdateBroadcast;
import com.tavuc.networking.models.EnemyRemovedBroadcast;
import com.tavuc.models.items.CoinDrop;
import com.tavuc.models.entities.enemies.Enemy;
import com.tavuc.models.space.BaseShip;   // Added import
import com.tavuc.ai.WaveManager;
import com.tavuc.ai.WaveConfiguration;
import com.tavuc.ai.EnemySpawnData;
import com.tavuc.ai.SpawnPattern;
import com.tavuc.models.entities.enemies.EnemyType;


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

    // Coin drop tracking
    private final ConcurrentMap<String, CoinDrop> coinDrops = new ConcurrentHashMap<>();
    private int nextCoinDropId = 1;


    // Wave and enemy tracking
    private WaveManager waveManager;
    private final List<Enemy> activeEnemies = new ArrayList<>();
    private boolean[][] blockedTiles;

    // Damage dealt by players while on the ground
    private static final double PLAYER_ATTACK_DAMAGE = 0.5;
    // Starting health for players when on the ground - changed to match client expectations
    private static final double PLAYER_START_HEALTH = 6.0; // Changed from 3.0 to 6.0
    // Cooldown between melee attacks (ms)
    private static final long PLAYER_ATTACK_COOLDOWN_MS = 300;

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

        // Set up wave manager with some basic waves
        waveManager = new WaveManager();
        blockedTiles = new boolean[100][100];
        List<WaveConfiguration> waves = new ArrayList<>();

        WaveConfiguration w1 = new WaveConfiguration();
        w1.setWaveNumber(1);
        w1.setTimeLimit(Duration.ofSeconds(5));
        w1.setSpawnPattern(SpawnPattern.PERIMETER);
        w1.setEnemies(List.of(new EnemySpawnData(EnemyType.TROOPER, 1)));

        WaveConfiguration w2 = new WaveConfiguration();
        w2.setWaveNumber(2);
        w2.setTimeLimit(Duration.ofSeconds(5));
        w2.setSpawnPattern(SpawnPattern.PERIMETER);
        w2.setEnemies(List.of(new EnemySpawnData(EnemyType.TROOPER, 2)));

        waves.add(w1);
        waves.add(w2);

        waveManager.setWaves(waves);
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
            int dropped = target.extractCoins();
            if (dropped > 0) {
                spawnCoinDrop(target.getX(), target.getY(), dropped);
            }
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
     * Handles force ability usage between two players. Currently only applies
     * a simple damage effect validated server-side.
     *
     * @param attackerId ID of the player using the ability
     * @param targetId   ID of the target player
     * @param ability    Name of the ability being used
     */
    public synchronized void handleForceAbility(int attackerId, int targetId, String ability) {
        Player attacker = null;
        Player targetPlayer = null;
        Enemy targetEnemy = null;
        for (Player p : playerSessions.keySet()) {
            if (p.getId() == attackerId) attacker = p;
            if (p.getId() == targetId) targetPlayer = p;
        }
        for (Enemy e : activeEnemies) {
            if (e.getId() == targetId) targetEnemy = e;
        }
        if (attacker == null) return;

        double range = attacker.getAttackRange();

        switch (ability) {
            case "FORCE_SLAM" -> {
                for (Player p : playerSessions.keySet()) {
                    if (p == attacker) continue;
                    double dx = p.getX() - attacker.getX();
                    double dy = p.getY() - attacker.getY();
                    if (Math.hypot(dx, dy) <= range) {
                        applyAbilityDamage(attacker, p, 1.0);
                    }
                }
                for (Enemy e : activeEnemies) {
                    double dx = e.getX() - attacker.getX();
                    double dy = e.getY() - attacker.getY();
                    if (Math.hypot(dx, dy) <= range) {
                        applyAbilityDamage(attacker, e, 1.0);
                    }
                }
            }
            case "FORCE_PUSH" -> {
                if (targetPlayer == null && targetEnemy == null) return;
                double dx = (targetPlayer != null ? targetPlayer.getX() : targetEnemy.getX()) - attacker.getX();
                double dy = (targetPlayer != null ? targetPlayer.getY() : targetEnemy.getY()) - attacker.getY();
                double dist = Math.hypot(dx, dy);
                if (dist > range) return;
                if (dist != 0) {
                    if (targetPlayer != null) {
                        targetPlayer.setDx(dx / dist * 5);
                        targetPlayer.setDy(dy / dist * 5);
                    } else {
                        targetEnemy.setDx(dx / dist * 5);
                        targetEnemy.setDy(dy / dist * 5);
                    }
                }
                if (targetPlayer != null) applyAbilityDamage(attacker, targetPlayer, 0.5);
                else applyAbilityDamage(attacker, targetEnemy, 0.5);
            }
            case "FORCE_CHOKE" -> {
                if (targetPlayer == null && targetEnemy == null) return;
                double dx = (targetPlayer != null ? targetPlayer.getX() : targetEnemy.getX()) - attacker.getX();
                double dy = (targetPlayer != null ? targetPlayer.getY() : targetEnemy.getY()) - attacker.getY();
                if (Math.hypot(dx, dy) > range) return;
                if (targetPlayer != null) applyAbilityDamage(attacker, targetPlayer, 1.5);
                else applyAbilityDamage(attacker, targetEnemy, 1.5);
            }
            default -> {
                if (targetPlayer != null) applyAbilityDamage(attacker, targetPlayer, 1.0);
                else if (targetEnemy != null) applyAbilityDamage(attacker, targetEnemy, 1.0);
            }
        }
    }

    private void applyAbilityDamage(Player attacker, Player target, double damage) {
        target.takeDamage(damage);

        PlayerDamagedBroadcast dmg = new PlayerDamagedBroadcast(
                target.getIdAsString(),
                damage,
                target.getHealth()
        );
        broadcastToGame(dmg);

        if (target.getHealth() <= 0) {
            int dropped = target.extractCoins();
            if (dropped > 0) {
                spawnCoinDrop(target.getX(), target.getY(), dropped);
            }
            PlayerKilledBroadcast killed = new PlayerKilledBroadcast(
                    target.getIdAsString(),
                    attacker.getIdAsString()
            );
            broadcastToGame(killed);
            removePlayer(target, playerSessions.get(target));
        }
    }

    private void applyAbilityDamage(Player attacker, Enemy target, double damage) {
        target.takeDamage(damage);
        if (target.getHealth() <= 0) {
            activeEnemies.remove(target);
            handleEnemyKilled(target, attacker.getId());
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
            Map<Player, int[]> prevPositions = new HashMap<>();

            // --- Enemy wave management ---
            if (waveManager != null && !players.isEmpty()) {
                // Remove defeated enemies and drop coins
                java.util.Iterator<Enemy> it = activeEnemies.iterator();
                while (it.hasNext()) {
                    Enemy e = it.next();
                    if (e.getHealth() <= 0) {
                        it.remove();
                        handleEnemyKilled(e, -1);
                        broadcastToGame(new EnemyRemovedBroadcast(String.valueOf(e.getId())));
                    }
                }

                // Spawn next wave if needed
                if ((activeEnemies.isEmpty() || waveManager.isCurrentWaveTimedOut()) && waveManager.hasMoreWaves()) {
                    Player target = players.get(0);
                    List<Enemy> spawned = waveManager.spawnNextWave(blockedTiles, target);
                    activeEnemies.addAll(spawned);
                    for (Enemy e : spawned) {
                        broadcastToGame(new EnemySpawnedBroadcast(
                                String.valueOf(e.getId()),
                                e.getClass().getSimpleName(),
                                e.getX(), e.getY(),
                                (int)e.getHealth(),
                                e.getWidth(), e.getHeight()));
                    }
                }

                // Update active enemies
                for (Enemy enemy : activeEnemies) {
                    enemy.update();
                    broadcastToGame(new EnemyUpdateBroadcast(
                            String.valueOf(enemy.getId()),
                            enemy.getX(), enemy.getY(),
                            enemy.getDx(), enemy.getDy(),
                            enemy.getDirection(),
                            enemy.getHealth()));
                }
            }

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

            // Check coin drop pickups
            for (Player player : players) {
                Rectangle hb = player.getHurtbox();
                for (CoinDrop drop : new ArrayList<>(coinDrops.values())) {
                    if (hb.intersects(drop.getHitBox())) {
                        player.addCoins(drop.getAmount());
                        ClientSession session = playerSessions.get(player);
                        if (session != null) {
                            session.sendMessage(new CoinUpdateBroadcast(player.getIdAsString(), player.getCoins()));
                        }
                        removeCoinDrop(drop.getId());
                    }
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

    /** Returns the list of currently active ground enemies. */
    public List<Enemy> getActiveEnemies() {
        return activeEnemies;
    }

    /** Retrieve a player by ID from the active player list. */
    private Player getPlayerById(int id) {
        for (Player p : playerSessions.keySet()) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    /** Spawn a coin drop at the given position. */
    private void spawnCoinDrop(int x, int y, int amount) {
        String id = "drop" + (nextCoinDropId++);
        CoinDrop drop = new CoinDrop(id, x, y, amount);
        coinDrops.put(id, drop);
        broadcastToGame(new CoinDropSpawnedBroadcast(id, x, y, amount));
    }

    /** Remove a coin drop and notify clients. */
    private void removeCoinDrop(String id) {
        if (coinDrops.remove(id) != null) {
            broadcastToGame(new CoinDropRemovedBroadcast(id));
        }
    }

    /** Expose current coin drops for testing. */
    public Map<String, CoinDrop> getCoinDrops() {
        return coinDrops;
    }

    /** Award coins to a player and notify them. */
    public synchronized void awardCoins(int playerId, int amount) {
        Player p = getPlayerById(playerId);
        if (p == null) return;
        p.addCoins(amount);
        ClientSession session = playerSessions.get(p);
        if (session != null) {
            session.sendMessage(new CoinUpdateBroadcast(p.getIdAsString(), p.getCoins()));
        }
    }

    /** Handle a player's extraction request. Coins are reset to zero. */
    public synchronized void handleExtraction(int playerId) {
        Player p = getPlayerById(playerId);
        if (p == null) return;
        p.extractCoins();
        ClientSession session = playerSessions.get(p);
        if (session != null) {
            session.sendMessage(new CoinUpdateBroadcast(p.getIdAsString(), p.getCoins()));
        }
    }

    /** Called when an enemy is killed to drop coins. */
    public synchronized void handleEnemyKilled(Enemy enemy, int killerId) {
        if (enemy == null) return;
        spawnCoinDrop(enemy.getX(), enemy.getY(), 5);
        if (killerId != -1) {
            Player killer = getPlayerById(killerId);
            if (killer != null) {
                // Optional future logic for kill rewards
            }
        }
    }
}
