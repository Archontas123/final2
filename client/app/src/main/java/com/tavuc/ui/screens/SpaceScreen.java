package com.tavuc.ui.screens;

import javax.swing.*;
import javax.swing.Timer;

import com.tavuc.ui.panels.SpacePanel;
import com.tavuc.ui.panels.SpaceScreenUILayer;
import com.tavuc.Client;
import com.tavuc.managers.InputManager;
import com.tavuc.managers.SpaceManager;
import com.tavuc.models.space.Ship;
import com.tavuc.models.space.Planet;
import java.util.List;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SpaceScreen extends GScreen {

    private int playerId;
    private String username;
    private JFrame previousFrame;
    private SpacePanel spacePanel;
    private SpaceScreenUILayer uiLayerPanel;
    private SpaceManager spaceManager;

    public SpaceScreen(JFrame previousFrame, int playerId, String username) {
        super();
        this.previousFrame = previousFrame;
        this.playerId = playerId;
        this.username = username;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.spaceManager = new SpaceManager(); 

        spacePanel = new SpacePanel(this, previousFrame, playerId, username, this.spaceManager); 
        Client.currentSpacePanel = spacePanel; 
        setScreenSpecificPanel(spacePanel);

        uiLayerPanel = new SpaceScreenUILayer(InputManager.getInstance()); // Pass InputManager
        addUILayer(uiLayerPanel);

        // if (uiLayerPanel.getActionButton() != null) { // Action button was removed
        //     for (ActionListener al : uiLayerPanel.getActionButton().getActionListeners()) {
        //         uiLayerPanel.getActionButton().removeActionListener(al);
        //     }
        //     uiLayerPanel.getActionButton().addActionListener(e -> {
        //         // Client.enterShipInterior(); // Removed ship interior functionality
        //     });
        // }

        if (!super.isActuallyFullScreen()) {
            setPreferredSize(new Dimension(1000, 700));
            pack();
            setLocationRelativeTo(previousFrame);
            if (titleBarPanel != null) titleBarPanel.setVisible(true);
        } else {
            if (titleBarPanel != null) titleBarPanel.updateMaximizeButtonText(super.isActuallyFullScreen());
        }
        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                if (uiLayerPanel != null && layeredPane != null) {
                    uiLayerPanel.setBounds(0, 0, getWidth(), getHeight());
                    layeredPane.revalidate();
                    layeredPane.repaint();
                }
            }
        });

        setVisible(true);
        if (spacePanel != null) {
            SwingUtilities.invokeLater(() -> spacePanel.requestFocusInWindow());
        }

    }

    public void updatePlayerCoordinatesOnUI(double x, double y) {
        if (uiLayerPanel != null) {
            uiLayerPanel.updateCoordinates(x, y);
        }
    }

    @Override
    public void performClose() {
        this.dispose();
    }

    @Override
    public String getScreenTitle() {
        return "Navigate Space"; 
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b && spacePanel != null) {
            SwingUtilities.invokeLater(() -> spacePanel.requestFocusInWindow());
        }
    }

    @Override
    public void performMaximizeRestore() {
        super.performMaximizeRestore();
        if (spacePanel != null) {
            SwingUtilities.invokeLater(() -> spacePanel.requestFocusInWindow());
        }
    }

    public SpacePanel getSpacePanel() {
        return spacePanel;
    }

    public SpaceManager getSpaceManager() {
        return spaceManager;
    }

    public void updateUILayerData(int playerX, int playerY,
                                  List<Planet> planets, List<Ship> ships, Ship playerShip,
                                  boolean wPressed, boolean aPressed, boolean sPressed, boolean dPressed,
                                  int healthPercent, int shieldPercent, String dialogText) {
        if (uiLayerPanel != null) {
            uiLayerPanel.updateCoordinates(playerX, playerY);
            uiLayerPanel.updateMinimapData(planets, ships, playerShip);
            uiLayerPanel.updateMovementKeys(wPressed, aPressed, sPressed, dPressed);
            uiLayerPanel.updateStatusBars(healthPercent, shieldPercent);
            if (dialogText != null) {
                uiLayerPanel.updateDialog(dialogText);
            }
        }
    }

    /**
     * Adds an overlay component above the main content.
     * Useful for game over panels or dialogs.
     */
    public void showOverlay(JComponent overlay) {
        overlay.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        layeredPane.add(overlay, JLayeredPane.PALETTE_LAYER);
        if (uiLayerPanel != null) {
            layeredPane.remove(uiLayerPanel);
        }
        layeredPane.revalidate();
        layeredPane.repaint();
        overlay.requestFocusInWindow();
    }

    /**
     * Removes a previously added overlay component.
     */
    public void removeOverlay(JComponent overlay) {
        layeredPane.remove(overlay);
        if (uiLayerPanel != null) {
            layeredPane.add(uiLayerPanel, JLayeredPane.PALETTE_LAYER);
        }
        layeredPane.revalidate();
        layeredPane.repaint();
    }
}
