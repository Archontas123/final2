package com.tavuc.managers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tavuc.exceptions.ServerStartException;
import com.tavuc.models.space.BaseShip;
import com.tavuc.models.space.PlayerShip;
import com.tavuc.networking.ClientSession;
import com.tavuc.networking.ClientSessionListener;
import com.tavuc.networking.models.BaseMessage;
import com.tavuc.networking.models.ProjectileSpawnedBroadcast;
import com.tavuc.networking.models.ShipLeftBroadcast;
import com.tavuc.networking.models.ShipUpdateBroadcast;

import com.tavuc.networking.models.AttackShipUpdateBroadcast; // Added import
import com.tavuc.networking.models.CruiserUpdateBroadcast; // Added import
import com.tavuc.models.entities.Player; // Added import

public class NetworkManager implements ClientSessionListener {

    private ServerSocket socket;
    private final AuthManager authManager;
    private final LobbyManager lobbyManager;
    private volatile boolean running = false;
    private ExecutorService clientExecutor;
    private final Set<ClientSession> sessions = ConcurrentHashMap.newKeySet();

    private final Map<String, BaseShip> activeEntityShips = new ConcurrentHashMap<>();

    public NetworkManager(AuthManager authManager, LobbyManager lobbyManager) {
        this.authManager = authManager;
        this.lobbyManager = lobbyManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public void startServer(int port) throws ServerStartException {
        try {
            socket = new ServerSocket(port);
            running = true;
            clientExecutor = Executors.newCachedThreadPool();
            System.out.println("NetworkService started on port " + port);

            new Thread(() -> {
                while (running && !socket.isClosed()) {
                    try {
                        Socket clientSocket = socket.accept();
                        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                        ClientSession clientSession = new ClientSession(clientSocket, this.authManager, this.lobbyManager, this);
                        clientSession.setSessionListener(this);
                        sessions.add(clientSession);
                        clientExecutor.submit(clientSession::start);
                    } catch (IOException e) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                        
                    }
                }
            }, "NetworkService-AcceptThread").start();
        } catch (IOException e) {
            running = false;
            throw new ServerStartException("Could not start server on port " + port, e);
        }
    }

    public void stopServer() {
        System.out.println("Stopping NetworkService...");
        running = false;
        Set<ClientSession> sessionsToClose = ConcurrentHashMap.newKeySet();
        sessionsToClose.addAll(sessions);
        for (ClientSession session : sessionsToClose) {
            session.close("Server shutting down");
        }
        clientExecutor.shutdown();
        try {
            if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                clientExecutor.shutdownNow();
                if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS))
                    System.err.println("Client handler pool did not terminate");
            }
        } catch (InterruptedException ie) {
            clientExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("NetworkService stopped.");
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    @Override
    public void onSessionClosed(ClientSession session) {
        sessions.remove(session);
        int playerId = session.getPlayerId();
        removePlayerShip(playerId, session); 
        System.out.println("Client session " + session.getSessionId() + " (Player ID: " + playerId + ") removed.");
    }

    public PlayerShip getPlayerShip(int playerId) {
        String playerShipEntityId = "player_" + playerId;
        BaseShip ship = activeEntityShips.get(playerShipEntityId);
        if (ship instanceof PlayerShip) {
            return (PlayerShip) ship;
        }
        return null;
    }

  
    public synchronized void updateShip(int playerId, double x, double y, double angle, double dx, double dy, boolean thrusting, boolean shouldBeInSpace, ClientSession sourceSession) {
        String playerShipEntityId = "player_" + playerId;
        PlayerShip playerShip = getPlayerShip(playerId);

        if (playerShip == null && shouldBeInSpace) {
            playerShip = new PlayerShip(playerShipEntityId, String.valueOf(playerId), (int)x, (int)y, 50, 50); 
            activeEntityShips.put(playerShipEntityId, playerShip);
            System.out.println("NetworkManager: New PlayerShip created for player " + playerId + " ID: " + playerShipEntityId);
        }
        
        if (playerShip != null) {
            if (shouldBeInSpace) {
                playerShip.setPosition((int)x, (int)y);
                playerShip.setOrientation((float)angle);
                playerShip.setVelocity((float)dx, (float)dy);
                // TODO: PlayerShip might need a setThrusting(boolean) if it affects animation/state
                broadcastShipUpdate(playerShip, sourceSession);
            } else { 
                if(activeEntityShips.containsKey(playerShipEntityId)){
                    activeEntityShips.remove(playerShipEntityId);
                    broadcastShipLeft(playerId, sourceSession);
                    System.out.println("NetworkManager: PlayerShip for player " + playerId + " removed (not in active space).");
                }
            }
        }
    }

    public synchronized void setShipLanded(int playerId, ClientSession sourceSession) {
        String playerShipEntityId = "player_" + playerId;
        BaseShip ship = activeEntityShips.remove(playerShipEntityId);
        if (ship != null) {
            broadcastShipLeft(playerId, sourceSession);
            System.out.println("NetworkManager: PlayerShip for player " + playerId + " set to LANDED.");
        }
    }

    public synchronized void setShipLaunched(int playerId, double x, double y, double angle, ClientSession sourceSession) {
        String playerShipEntityId = "player_" + playerId;
        PlayerShip playerShip = getPlayerShip(playerId);
        if (playerShip == null) {
            playerShip = new PlayerShip(playerShipEntityId, String.valueOf(playerId), (int)x, (int)y, 50, 50); 
            activeEntityShips.put(playerShipEntityId, playerShip);
        } else { 
            playerShip.setPosition((int)x, (int)y);
            playerShip.setOrientation((float)angle);
            playerShip.setVelocity(0,0); 
        }
        broadcastShipUpdate(playerShip, sourceSession);
        System.out.println("NetworkManager: PlayerShip for player " + playerId + " LAUNCHED.");
    }
    
    public synchronized void removePlayerShip(int playerId, ClientSession sourceSession) {
        String playerShipEntityId = "player_" + playerId;
        BaseShip removedShip = activeEntityShips.remove(playerShipEntityId);
        if (removedShip != null) {
            broadcastShipLeft(playerId, sourceSession);
            System.out.println("NetworkManager: PlayerShip for player " + playerId + " removed due to session closure.");
        }
    }
    
    public synchronized void addOrUpdateNonPlayerShip(BaseShip ship) {
        if (ship == null || ship.getEntityId() == null) return;
        activeEntityShips.put(ship.getEntityId(), ship);
        // TODO: Implement broadcasting for AI ship updates (AttackShip, LightCruiser)
    }

    public synchronized void removeNonPlayerShip(String entityId) {
        BaseShip removedShip = activeEntityShips.remove(entityId);
        if (removedShip != null) {
            // TODO: Broadcast AI ship removal (e.g., EntityRemovedBroadcast)
            System.out.println("NetworkManager: AI Ship " + entityId + " removed.");
        }
    }
    
    public Map<String, BaseShip> getActiveEntityShips() {
        return activeEntityShips;
    }

    public Set<ClientSession> getActiveSessions() {
        return sessions;
    }

    public synchronized void ensureSingleSessionForPlayer(int playerId, String currentSessionId) {
        if (playerId == 0) return;
        for (ClientSession session : sessions) {
            if (session.getPlayerId() == playerId) {
                if (!session.getSessionId().equals(currentSessionId)) {
                    System.out.println("NetworkManager: Found stale session " + session.getSessionId() + " for player " + playerId + ". Closing it.");
                    session.close("Newer session logged in for this player.");
                }
            }
        }
    }

    private void broadcastShipUpdate(BaseShip ship, ClientSession sourceSession) {
        if (ship instanceof PlayerShip) {
            PlayerShip ps = (PlayerShip) ship;
            ShipUpdateBroadcast broadcastMessage = new ShipUpdateBroadcast(
                ps.getPlayerId(), 
                ps.getX(),
                ps.getY(),
                ps.getOrientation(),
                ps.getVelocityX(),
                ps.getVelocityY(),
                false 
            );
            broadcastMessageToAllActiveSessions(broadcastMessage);
        } 
   
       
    }

  
    private void broadcastShipLeft(int playerId, ClientSession sourceSession) {
        ShipLeftBroadcast broadcastMessage = new ShipLeftBroadcast(String.valueOf(playerId));
        broadcastMessageToAllActiveSessions(broadcastMessage);
    }

    public void broadcastMessageToAllActiveSessions(BaseMessage message) {
        for (ClientSession session : sessions) {
            if (session.getPlayerId() != 0) { 
                session.sendMessage(message);
            }
        }
    }

    public void broadcastMessageToAllActiveSessions(String message) { 
        for (ClientSession session : sessions) {
            if (session.getPlayerId() != 0) { 
                session.sendRawMessage(message); 
            }
        }
    }

  
}
