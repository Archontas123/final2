package com.tavuc.ui.screens;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.WindowConstants;

import com.tavuc.Client;
import com.tavuc.ui.panels.ShipInteriorPanel;
import com.tavuc.ui.panels.ShipInteriorUILayer;

public class ShipInteriorScreen extends GScreen implements Runnable {

    private int playerId;
    private String username;
    private ShipInteriorPanel shipInteriorPanel;
    private ShipInteriorUILayer uiLayer;
    private Thread gameLoopThread;
    private volatile boolean running = false;

    public ShipInteriorScreen(int playerId, String username) {
        super();
        this.playerId = playerId;
        this.username = username;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.shipInteriorPanel = new ShipInteriorPanel(playerId, username);
        setScreenSpecificPanel(shipInteriorPanel);

        this.uiLayer = new ShipInteriorUILayer();
        addUILayer(uiLayer);

     
        
        if (!super.isActuallyFullScreen()) {
            setPreferredSize(new Dimension(1280, 720));
            pack();
            setLocationRelativeTo(null);
            if (titleBarPanel != null) titleBarPanel.setVisible(true);
        } else {
             if (titleBarPanel != null) titleBarPanel.updateMaximizeButtonText(super.isActuallyFullScreen());
        }
    }

    public void startGameLoop() {
        if (gameLoopThread == null) {
            running = true;
            gameLoopThread = new Thread(this, "ShipInteriorLoop");
            gameLoopThread.start();
        }
    }

    public void stopGameLoop() {
        running = false;
        if (shipInteriorPanel != null) {
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
                frames = 0;
            }

            try {
                long sleepTime = (long) (lastTime - System.nanoTime() + ns) / 1000000;
                Thread.sleep(Math.max(0, sleepTime));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false; 
                System.err.println("Ship Interior game loop interrupted.");
            }
        }
    }

    private void updateGame() {
        if (shipInteriorPanel != null) {
            shipInteriorPanel.update();
        }
    }

    private void renderGame() {
        if (shipInteriorPanel != null) {
            shipInteriorPanel.repaint();
        }
    }
    
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            if (!running) {
                startGameLoop();
            }
            if (shipInteriorPanel != null) {
                javax.swing.SwingUtilities.invokeLater(() -> shipInteriorPanel.requestFocusInWindow());
            }
        } else {
            if (running) {
                stopGameLoop();
            }
        }
    }

    public int getPlayerId() {
        return playerId;
    }

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
        return "Ship Interior - " + username;
    }

    @Override
    public void performMaximizeRestore() {
        super.performMaximizeRestore(); 
        if (shipInteriorPanel != null) {
            shipInteriorPanel.requestFocusInWindow(); 
        }
    }
}
