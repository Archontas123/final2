package com.tavuc;

import java.util.List;

import com.tavuc.exceptions.ServerStartException;
import com.tavuc.managers.AuthManager;
import com.tavuc.managers.GameManager;
import com.tavuc.managers.LobbyManager;
import com.tavuc.managers.NetworkManager; 

public class Server {

    private static NetworkManager networkManager; 
    private final AuthManager authManager;
    private final LobbyManager gameLobbyService; 
    private volatile boolean running = true; 

    /**
     * Gets the static instance of NetworkManager.
     * @return The NetworkManager instance.
     */
    public static NetworkManager getNetworkManager() {
        return networkManager;
    }

    // TEMPORARY: This is not a good way to get GameManager.
    // AttackShips should ideally get their GameManager instance via their parent or game context.
    public static GameManager getGameManager() {
        if (networkManager != null && networkManager.getLobbyManager() != null) {
            List<GameManager> games = networkManager.getLobbyManager().getActiveGameServices();
            if (games != null && !games.isEmpty()) {
                // THIS IS A PLACEHOLDER AND LIKELY INCORRECT FOR MULTIPLE GAME INSTANCES.
                // It returns the first game found, which might not be the one the AttackShip belongs to.
                // A proper solution would involve passing the specific GameManager to the AttackShip
                // or retrieving it based on a game ID associated with the AttackShip or its parent.
                return games.get(0); 
            }
        }
        return null;
    }

    /**
     * Constructor for the Server class.
     * Initializes the authentication service, game lobby service, and network service.
     * Starts the server on the specified port and begins the game loop.
     * @param port The port number on which the server will listen for incoming connections.
     */
    public Server(int port) {
        this.authManager = new AuthManager();
        this.gameLobbyService = new LobbyManager();
        networkManager = new NetworkManager(this.authManager, this.gameLobbyService);
        this.gameLobbyService.initializeLobby(networkManager); 
        
        try {
            networkManager.startServer(port);
            System.out.println("Server application initialized and network service started on port " + port);
            startGameLoop(); 
        } catch (ServerStartException e) {
            System.err.println("Failed to start the server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); 
        }
    }

    /**
     * Starts the main game loop in a separate thread.
     * The loop runs at a target ticks per second (TPS) rate, updating game states accordingly.
     */
    private void startGameLoop() {
        new Thread(() -> {
            long lastTime = System.nanoTime();
            double amountOfTicks = 60.0; 
            double ns = 1000000000 / amountOfTicks;
            double delta = 0;
            long timer = System.currentTimeMillis();

            while (running) {
                long now = System.nanoTime();
                delta += (now - lastTime) / ns;
                lastTime = now;

                while (delta >= 1) {
                    tick();
                    delta--;
                }

                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000;
                }

                try {
                    long sleepTime = (long) (lastTime - System.nanoTime() + ns) / 1000000;
                    Thread.sleep(Math.max(0, sleepTime));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Game loop interrupted.");
                    running = false; 
                }
            }
            System.out.println("Game loop stopped.");
        }, "MainGameLoopThread").start();
    }

    /**
     * Main game loop tick method.
     * Iterates through all active game services and updates their states.
     * Also updates the LobbyManager.
     */
    private void tick() {
        try {
            gameLobbyService.update();
        } catch (Exception e) {
            System.err.println("Error updating LobbyManager: " + e.getMessage());
            e.printStackTrace();
        }
        
        List<GameManager> activeGames = gameLobbyService.getActiveGameServices();
        if (activeGames != null) {
            for (GameManager game : activeGames) {
                try {
                    game.update();
                } catch (Exception e) {
                    System.err.println("Error updating game " + game.getGameId() + " (" + game.getPlanetName() + "): " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Stops the network service and sets the running flag to false.
     */
    public void shutdown() {
        System.out.println("Shutting down server...");
        this.running = false; 
        networkManager.stopServer();
        System.out.println("Server shutdown complete.");
    }

    public static void main(String[] args) {
        int port = 5000; 
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number specified: " + args[0] + ". Using default port " + port);
            }
        }
        new Server(port); 
    }
}
