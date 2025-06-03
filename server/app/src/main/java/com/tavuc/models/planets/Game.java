package com.tavuc.models.planets;

import java.util.List;

import com.tavuc.models.entities.Player;
import com.tavuc.networking.ClientSession;

import java.util.ArrayList;


public class Game {
    private int gameId;
    private String gameName; 
    private int currentPlayerCount;
    private int maxPlayerCount; 
    private String planetName;
    private Planet planet; 
    private List<Player> players; 
    private List<ClientSession> clientSessions; 

    /**
     * Constructor for Game
     * @param gameId Unique identifier for the game
     * @param gameName Name of the game
     * @param planetName Name of the planet associated with the game
     * @param maxPlayerCount Maximum number of players allowed in the game
     * @param planet The planet object associated with this game
     */
    public Game(int gameId, String gameName, String planetName, int maxPlayerCount, Planet planet) {
        this.gameId = gameId;
        this.gameName = gameName; 
        this.planetName = planetName;
        this.currentPlayerCount = 0; 
        this.maxPlayerCount = maxPlayerCount;
        this.planet = planet;
        this.players = new ArrayList<>();
        this.clientSessions = new ArrayList<>();
    }

    /**
     * Gets the unique identifier for the game.
     * @return The game ID.
     */
    public int getGameId() { 
        return gameId; 
    }

    /**
     * Gets the name of the game.
     * @return The name of the game.
     */
    public String getGameName() { 
        return gameName; 
    }

    /**
     * Gets the name of the planet associated with the game.
     * @return The name of the planet.
     */
    public String getPlanetName() { 
        return planetName; 
    }

    /**
     * Gets the current number of players in the game.
     * @return The current player count.
     */
    public int getCurrentPlayerCount() { 
        return players.size(); 
    } 

    /**
     * Gets the maximum number of players allowed in the game.
     * @return The maximum player count.
     */
    public int getMaxPlayerCount() { 
        return maxPlayerCount; 
    }

    /**
     * Gets the Planet object associated with this game.
     * @return The Planet object.
     */
    public Planet getPlanet() { 
        return planet; 
    }

    /**
     * Gets the list of players currently in the game.
     * @return A list of Player objects representing the players in the game.
     */
    public List<Player> getPlayers() { 
        return players; 
    }

    /**
     * Gets the list of client sessions associated with the players in this game.
     * @return A list of ClientSession objects representing the client sessions of players in the game.
     */
    public List<ClientSession> getClientSessions() { 
        return clientSessions; 
    }

    /**
     * Adds a player to the game if there is space available.
     * @param player The player to be added.
     * @param session The client session of the player to be added.
     * @return True if the player was added successfully, false if the game is full or the player is already in the game.
     */
    public synchronized boolean addPlayer(Player player, ClientSession session) {
        if (players.size() < maxPlayerCount) {
            if (!players.contains(player) && !clientSessions.contains(session)) {
                players.add(player);
                clientSessions.add(session);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a player from the game.
     * @param player The player to be removed.
     * @param session The client session of the player to be removed.
     */
    public synchronized void removePlayer(Player player, ClientSession session) {
        players.remove(player);
        clientSessions.remove(session);
    }

    /**
     * Returns a string representation of the game, including its ID, planet name, and player counts.
     */
    @Override
    public String toString() {
        return gameId + ":" + planetName + ":" + getCurrentPlayerCount() + "/" + maxPlayerCount; 
    }
}
