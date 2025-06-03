package com.tavuc.ui.screens;

import javax.swing.*;
import javax.swing.Timer; 

import com.tavuc.ui.panels.SpacePanel;
import com.tavuc.ui.panels.SpaceScreenUILayer;
import com.tavuc.Client;
import com.tavuc.managers.SpaceManager;
import com.tavuc.models.space.Ship; 

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

        uiLayerPanel = new SpaceScreenUILayer();
        addUILayer(uiLayerPanel);

        if (uiLayerPanel.getActionButton() != null) {
            for (ActionListener al : uiLayerPanel.getActionButton().getActionListeners()) {
                uiLayerPanel.getActionButton().removeActionListener(al);
            }
            uiLayerPanel.getActionButton().addActionListener(e -> {
                Client.enterShipInterior(); 
            });
        }

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
}
