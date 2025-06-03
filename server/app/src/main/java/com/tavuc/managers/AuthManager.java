package com.tavuc.managers;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tavuc.exceptions.AuthenticationException;
import com.tavuc.exceptions.RegistrationException;
import com.tavuc.models.entities.Player;

public class AuthManager {

    private static final String ID_COUNTER_FILE = "app/src/main/resources/player_id_counter.txt";
    private final Map<String, Player> onlinePlayers = new ConcurrentHashMap<>();

    /**
     * Constructor for AuthenticationService
     * Initializes the player resource directories if they do not exist.
     */
    public AuthManager() {
        try {
            Path playerDir = Paths.get("app/src/main/resources/players");
            if (!Files.exists(playerDir)) Files.createDirectories(playerDir);

            Path counterFileDir = Paths.get(ID_COUNTER_FILE).getParent();
            if (!Files.exists(counterFileDir)) Files.createDirectories(counterFileDir);
            
        } catch (IOException e) {
            System.err.println("Failed to create/find player resource directories: " + e.getMessage());
        }
    }

    /**
     * Gets the next available player ID by reading from a counter file.
     * The counter file is updated atomically to ensure thread safety.
     * @return The next available player ID.
     * @throws IOException If there is an error reading or writing the counter file.
     */
    private synchronized int getNextPlayerId() throws IOException {
        Path counterFilePath = Paths.get(ID_COUNTER_FILE);
        
        Path parentDir = counterFilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        int currentIdToAssign;
        if (Files.exists(counterFilePath) && Files.size(counterFilePath) > 0) {
            try (BufferedReader reader = Files.newBufferedReader(counterFilePath)) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) {
                    try {
                        int lastAssignedId = Integer.parseInt(line.trim());
                        currentIdToAssign = lastAssignedId + 1;
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing player ID counter from file: " + counterFilePath.toAbsolutePath() + ". Content: \"" + line + "\". Resetting to 1. Error: " + e.getMessage());
                        currentIdToAssign = 1; 
                    }
                } else {
                    currentIdToAssign = 1; 
                }
            }
        } else {
            currentIdToAssign = 1; 
        }

        try (BufferedWriter writer = Files.newBufferedWriter(counterFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write(String.valueOf(currentIdToAssign));
        }
        
        return currentIdToAssign;
    }

    /**
     * Registers a new player with the given username and password.
     * @param username The desired username for the new player.
     * @param password The desired password for the new player.
     * @return The newly registered Player object.
     * @throws RegistrationException If registration fails due to invalid input or existing username.
     */
    public Player register(String username, String password) throws RegistrationException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new RegistrationException("Username and password both cannot be empty.");
        }

        if (Player.load(username.trim()) != null) {
            throw new RegistrationException("Username already exists: " + username.trim());
        }

        int newPlayerId;
        try {
            newPlayerId = getNextPlayerId();
        } catch (IOException e) {
            System.err.println("Failed to get next player ID: " + e.getMessage());
            throw new RegistrationException("Server error during registration: Could not generate player ID.", e);
        }

        Player newPlayer = new Player(newPlayerId, username, password);
        try {
            newPlayer.save();
        } catch (Exception e) {
            System.err.println("Failed to save new player data: " + e.getMessage());
            throw new RegistrationException("Server error during registration: Could not save player data.", e);
        }
        
        System.out.println("Player registered: " + username + " with ID: " + newPlayerId);
        return newPlayer;
    }

    /**
     * Logs in a player with the given username and password.
     * @param username The username of the player trying to log in.
     * @param password The password of the player trying to log in.
     * @return The authenticated Player object.
     * @throws AuthenticationException If authentication fails due to invalid input, incorrect credentials, or player already logged in.
     */
    public Player login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Username and password cannot be empty");
            throw new AuthenticationException("Username and password cannot be empty.");
        }

        username = username.trim();
        Player player = Player.load(username);

        if (player == null) {
            System.err.println("Login failed: Player not found - " + username);
            throw new AuthenticationException("Login failed: Player not found - " + username);
        }

        String storedPassword = player.getPassword();
        storedPassword = (storedPassword == null) ? "" : storedPassword.trim();
        String inputPassword = password.trim();


        if (!storedPassword.equals(inputPassword)) {
            System.err.println("Login failed: Incorrect password for " + username);
            throw new AuthenticationException("Login failed: Incorrect password for " + username);
        }
        
        if (onlinePlayers.containsKey(username)) {
            System.err.println("Login failed: Player " + username + " is already logged in.");
            throw new AuthenticationException("Login failed: Player " + username + " is already logged in.");
        }

        onlinePlayers.put(username, player);
        System.out.println("Player authenticated and logged in: " + username);
        return player;
    }

    /**
     * Logs out a player, removing them from the online players list.
     * @param player The Player object representing the player to log out.
     */
    public void logout(Player player) {
        onlinePlayers.remove(player.getUsername());
        System.out.println("Player logged out: " + player.getUsername());
        
    }
}
