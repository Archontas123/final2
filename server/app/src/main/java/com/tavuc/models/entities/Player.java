package com.tavuc.models.entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Player extends Entity {
    
    private String username;
    private String password;
    private double directionAngle; 
    private double lastSpaceX;
    private double lastSpaceY;
    private double lastSpaceAngle;
    private double attackRange = 40.0;

    /**
     * Constructor for Player
     * @param id player ID
     * @param username players username
     * @param password players password
     */
    public Player(int id, String username, String password) {
        // Players start with 6 half-hearts (3 full hearts) while on the ground
        // This matches the client expectation of 6 health units
        super(id, username.trim(), 0, 0, 6.0, 60, 60); // Changed from 3.0 to 6.0
        this.username = username.trim();
        this.password = password;
        this.directionAngle = 0.0; 
        this.lastSpaceX = 0.0; // Default to 0,0 or a system entry point
        this.lastSpaceY = 0.0;
        this.lastSpaceAngle = 0.0;
    }

    /** 
     * Gets the player username
     * @return players username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the players password
     * @return players password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the direction angle of the player.
     * @return The direction angle in radians.
     */
    public double getDirectionAngle() {
        return directionAngle;
    }

    /**
     * Sets the direction angle of the player.
     * @param directionAngle The new direction angle in radians.
     */
    public void setDirectionAngle(double directionAngle) {
        this.directionAngle = directionAngle;
    }

    public double getLastSpaceX() {
        return lastSpaceX;
    }

    public void setLastSpaceX(double lastSpaceX) {
        this.lastSpaceX = lastSpaceX;
    }

    public double getLastSpaceY() {
        return lastSpaceY;
    }

    public void setLastSpaceY(double lastSpaceY) {
        this.lastSpaceY = lastSpaceY;
    }

    public double getLastSpaceAngle() {
        return lastSpaceAngle;
    }

    public void setLastSpaceAngle(double lastSpaceAngle) {
        this.lastSpaceAngle = lastSpaceAngle;
    }

    /**
     * Gets the melee attack range for this player.
     */
    public double getAttackRange() {
        return attackRange;
    }

    /**
     * Sets the melee attack range for this player.
     */
    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    public String getIdAsString() {
        return String.valueOf(this.getId());
    }

    /**
     * 
     */
    public void save() {
        Path playerDir = Paths.get("app", "src", "main", "resources", "players");
        try {
            if (!Files.exists(playerDir)) {
                Files.createDirectories(playerDir);
            }
            Path playerFile = playerDir.resolve(username + ".txt");

            Properties props = new Properties();
            props.setProperty("id", String.valueOf(this.getId()));
            props.setProperty("username", this.username);
            if (this.password != null) {
                props.setProperty("password", this.password);
            } else {
                props.setProperty("password", "");
            }
            props.setProperty("lastSpaceX", String.valueOf(this.lastSpaceX));
            props.setProperty("lastSpaceY", String.valueOf(this.lastSpaceY));
            props.setProperty("lastSpaceAngle", String.valueOf(this.lastSpaceAngle));

            try (BufferedWriter writer = Files.newBufferedWriter(playerFile)) {
                props.store(writer, "Player data");
            }
        } catch (IOException e) {
            System.err.println("Failed to save player data for " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads a player from a file
     * @param username The username of the player to load
     * @return The loaded Player object, or null if loading fails
     */
    public static Player load(String username) {
        Path playerFile = Paths.get("app", "src", "main", "resources", "players", username + ".txt");
        System.out.println("[DEBUG Player.load] Attempting to load player: '" + username + "' from file: '" + playerFile.toAbsolutePath().toString() + "'"); 
        Properties props = new Properties();

        if (!Files.exists(playerFile)) {
            System.out.println("[DEBUG Player.load] Player file NOT found for: '" + username + "' at '" + playerFile.toAbsolutePath().toString() + "'"); 
            return null;
        }
        System.out.println("[DEBUG Player.load] Player file FOUND for: '" + username + "'"); 

        try (BufferedReader reader = Files.newBufferedReader(playerFile)) {
            props.load(reader);
            String idStr = props.getProperty("id", "0"); 
            String passwordFromFile = props.getProperty("password"); 
            System.out.println("[DEBUG Player.load] For '" + username + "': id from file='" + idStr + "', password from file='" + passwordFromFile + "'"); 

            int id = Integer.parseInt(idStr); 
            Player player = new Player(id, username, passwordFromFile);
            player.setLastSpaceX(Double.parseDouble(props.getProperty("lastSpaceX", "0.0")));
            player.setLastSpaceY(Double.parseDouble(props.getProperty("lastSpaceY", "0.0")));
            player.setLastSpaceAngle(Double.parseDouble(props.getProperty("lastSpaceAngle", "0.0")));
            return player;
        } catch (IOException e) {
            System.err.println("[DEBUG Player.load] IOException for '" + username + "': " + e.getMessage()); 
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            System.err.println("[DEBUG Player.load] NumberFormatException for ID '" + props.getProperty("id") + "' for user '" + username + "': " + e.getMessage()); 
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if two players are equal based on their ID.
     * @param o The object to compare with
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return this.getId() == player.getId();
    }

    /**
     * Returns the hash code for the player based on its ID.
     * This is used to ensure that players can be used in hash-based collections.
     * @return The hash code of the player
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(this.getId());
    }

}
