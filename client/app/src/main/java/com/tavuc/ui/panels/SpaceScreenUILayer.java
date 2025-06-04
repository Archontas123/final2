package com.tavuc.ui.panels;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent; // For InputManager key codes
import java.util.List;

import com.tavuc.managers.InputManager;
import com.tavuc.models.space.Planet;
import com.tavuc.models.space.Ship;
import com.tavuc.ui.components.MinimapComponent;
import com.tavuc.ui.components.MovementKeysComponent;
import com.tavuc.ui.components.StatusBarsComponent;
import com.tavuc.ui.components.DialogComponent;

public class SpaceScreenUILayer extends JPanel {

    private MinimapComponent minimapComponent;
    private MovementKeysComponent movementKeysComponent;
    private StatusBarsComponent statusBarsComponent;
    private DialogComponent dialogComponent;

    private static final int UI_PADDING = 10;

    public SpaceScreenUILayer(InputManager inputManager) {
        setOpaque(false);
        setFocusable(false);
        setLayout(null); // Use absolute positioning

        minimapComponent = new MinimapComponent();
        add(minimapComponent);

        statusBarsComponent = new StatusBarsComponent();
        add(statusBarsComponent);
        
        dialogComponent = new DialogComponent();
        add(dialogComponent);

        movementKeysComponent = new MovementKeysComponent(inputManager);
        add(movementKeysComponent);
    }

    @Override
    public void doLayout() {
        super.doLayout();

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int topOffset = 0;

        java.awt.Window windowAncestor = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (windowAncestor instanceof com.tavuc.ui.screens.GScreen) {
            com.tavuc.ui.screens.GScreen gScreen = (com.tavuc.ui.screens.GScreen) windowAncestor;
            com.tavuc.ui.panels.TitleBar titleBar = gScreen.getTitleBarPanel();
            if (titleBar != null && titleBar.isVisible()) {
                topOffset = titleBar.getHeight();
            }
        }

        if (statusBarsComponent != null) {
            Dimension statusBarsSize = statusBarsComponent.getPreferredSize();
            statusBarsComponent.setBounds(UI_PADDING, topOffset + UI_PADDING, statusBarsSize.width, statusBarsSize.height);
        }

        if (minimapComponent != null) {
            Dimension minimapSize = minimapComponent.getPreferredSize();
            minimapComponent.setBounds(panelWidth - minimapSize.width - UI_PADDING, topOffset + UI_PADDING, minimapSize.width, minimapSize.height);
        }
        
        if (dialogComponent != null) {
            Dimension dialogSize = dialogComponent.getPreferredSize();
            dialogComponent.setBounds(UI_PADDING, panelHeight - dialogSize.height - UI_PADDING, dialogSize.width, dialogSize.height);
        }

        if (movementKeysComponent != null) {
            Dimension movementKeysSize = movementKeysComponent.getPreferredSize();
            movementKeysComponent.setBounds(panelWidth - movementKeysSize.width - UI_PADDING, panelHeight - movementKeysSize.height - UI_PADDING, movementKeysSize.width, movementKeysSize.height);
        }
    }

    public void updateCoordinates(double x, double y) {
        if (minimapComponent != null) { // Coordinates are now part of MinimapComponent
            minimapComponent.updateCoordinates((int) x, (int) y);
        }
    }

    public void updateMinimapData(List<Planet> planets, List<Ship> ships, Ship playerShip) {
        if (minimapComponent != null) {
            minimapComponent.updateMinimapData(planets, ships, playerShip);
        }
    }

    public void updateMovementKeys(boolean wPressed, boolean aPressed, boolean sPressed, boolean dPressed) {
        if (movementKeysComponent != null) {
            movementKeysComponent.updateKeyStates(wPressed, aPressed, sPressed, dPressed);
        }
    }

    public void updateStatusBars(int health, int shield) {
        if (statusBarsComponent != null) {
            statusBarsComponent.setHealth(health);
            statusBarsComponent.setShield(shield);
        }
    }

    public void updateDialog(String text) {
        if (dialogComponent != null) {
            dialogComponent.setDialogText(text);
        }
    }
}
