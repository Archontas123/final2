package com.tavuc.ui.screens;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.WindowConstants;

import com.tavuc.Client;
import com.tavuc.ui.panels.GamePanel;
import com.tavuc.ui.panels.GameScreenUILayer;


public class GameScreen extends GScreen implements Runnable {

    private int playerId;
    private String username;
    private int gameId;
    private String planetName;
    private GamePanel gamePanel;
    private GameScreenUILayer uiLayer;
    private Thread gameLoopThread;
    private volatile boolean running = false;

    public GameScreen(int playerId, String username, int gameId, String planetName) {
        super();
        this.playerId = playerId;
        this.username = username;
        this.gameId = gameId;
        this.planetName = planetName;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.gamePanel = new GamePanel(playerId, username, gameId);
        setScreenSpecificPanel(gamePanel);

        this.uiLayer = new GameScreenUILayer();
        addUILayer(uiLayer);

        uiLayer.getActionButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Client.returnToShip();
            }
        });
        
        if (!super.isActuallyFullScreen()) {
            setPreferredSize(new Dimension(1280, 720));
            pack();
            setLocationRelativeTo(null);
            if (titleBarPanel != null) titleBarPanel.setVisible(true);
        } else {
             if (titleBarPanel != null) titleBarPanel.updateMaximizeButtonText(super.isActuallyFullScreen());
        }
    }


    /**
     * Starts the game loop in a separate thread.
     * This method is called when the game screen is made visible.
     */
    public void startGameLoop() {
        if (gameLoopThread == null) {
            running = true;
            gameLoopThread = new Thread(this, "ClientGameLoop");
            gameLoopThread.start();
        }
    }

    /**
     * Stops the game loop and cleans up resources.
     * This method is called when the game screen is closed or made invisible.
     */
    public void stopGameLoop() {
        running = false;
        if (gamePanel != null) {
            gamePanel.stopGame(); 
        }
        if (gameLoopThread != null) {
            try {
                gameLoopThread.join(); 
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            gameLoopThread = null;
        }
    }

    /**
     * The main game loop that runs at a fixed tick rate.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0; 
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0; 

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                updateGame(); 
                delta--;
            }

            renderGame(); 
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("Client FPS: " + frames);
                frames = 0;
            }

            try {
        
                long sleepTime = (long) (lastTime - System.nanoTime() + ns) / 1000000;
                Thread.sleep(Math.max(0, sleepTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false; 
                System.err.println("Client game loop interrupted.");
            }
        }
    }


    /**
     * Updates the game state.
     * This method is called on each tick of the game loop.
     */
    private void updateGame() {}

    /**
     * Renders the game panel.
     * This method is called on each tick of the game loop to update the display.
     */
    private void renderGame() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
    
    /**
     * Sets the visibility of the game screen and starts or stops the game loop accordingly.
     * @param b true to make the screen visible, false to hide it.
     */
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            if (!running) {
                startGameLoop();
            }
            if (gamePanel != null) {
                javax.swing.SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
            }
        } else {
            if (running) {
                stopGameLoop();
            }
        }
    }

    /**
     * Gets the player ID.
     * @return the player ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Gets the player username.
     * @return the players username
     */
    public String getUsername() {
        return username;
    }

 
    @Override
    public void performClose() {
        stopGameLoop();
        this.dispose(); 
    }
    
    @Override
    public String getScreenTitle() {
        return "Game - " + planetName;
    }

    @Override
    public void performMaximizeRestore() {
        super.performMaximizeRestore(); 
        if (gamePanel != null) {
            gamePanel.requestFocusInWindow(); 
        }
    }
}
