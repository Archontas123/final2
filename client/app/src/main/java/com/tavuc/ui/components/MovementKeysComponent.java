package com.tavuc.ui.components;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import com.tavuc.managers.InputManager;

public class MovementKeysComponent extends JPanel {
    private JButton wButton, aButton, sButton, dButton;
    private InputManager inputManager;

    // Styled colors (consistent with other styled components)
    // COMPONENT_BG_COLOR is removed as the main container will be transparent
    private static final Color BORDER_COLOR = new Color(70, 75, 85);
    // ACCENT_COLOR removed as title is gone
    private static final Color TEXT_COLOR = new Color(200, 200, 210);

    private static final Color KEY_IDLE_BG_COLOR = new Color(45, 50, 65);
    private static final Color KEY_ACTIVE_BG_COLOR = new Color(120, 170, 190); 
    private static final Color KEY_ACTIVE_FG_COLOR = new Color(20, 25, 40); 

    private static final Font UI_FONT_BOLD = new Font("Serif", Font.BOLD, 16); 
    private static final Font KEY_FONT = new Font("Serif", Font.BOLD, 20); 

    private static final int BUTTON_SIZE = 50;
    private static final int PADDING = 10;
    private static final int TITLE_AREA_HEIGHT = 30;
    private static final int BORDER_THICKNESS = 2;
    private static final int ROUND_CORNER_ARC = 10;
    private static final int GRID_GAP = 5;

    public MovementKeysComponent(InputManager inputManager) {
        this.inputManager = inputManager;
        setOpaque(false); // Main component is transparent
        setLayout(new java.awt.BorderLayout()); 

        // Calculate preferred size based on the keys grid only
        int internalGridWidth = BUTTON_SIZE * 3 + GRID_GAP * 2;
        int internalGridHeight = BUTTON_SIZE * 2 + GRID_GAP * 1;
        // PADDING will be applied directly to keysPanel
        setPreferredSize(new Dimension(internalGridWidth + 2 * PADDING, internalGridHeight + 2 * PADDING));

        JPanel keysPanel = new JPanel(new GridLayout(2, 3, GRID_GAP, GRID_GAP));
        keysPanel.setOpaque(false); 
        // Border now only provides padding around the keys
        keysPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        wButton = createKeyButton("W", KeyEvent.VK_W);
        aButton = createKeyButton("A", KeyEvent.VK_A);
        sButton = createKeyButton("S", KeyEvent.VK_S);
        dButton = createKeyButton("D", KeyEvent.VK_D);

        JPanel emptyTopLeft = new JPanel(); emptyTopLeft.setOpaque(false);
        JPanel emptyTopRight = new JPanel(); emptyTopRight.setOpaque(false);

        keysPanel.add(emptyTopLeft); 
        keysPanel.add(wButton);
        keysPanel.add(emptyTopRight); 
        keysPanel.add(aButton);
        keysPanel.add(sButton);
        keysPanel.add(dButton);
        
        add(keysPanel, java.awt.BorderLayout.CENTER);
    }

    private JButton createKeyButton(String text, int keyCode) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        button.setFont(KEY_FONT);
        button.setBackground(KEY_IDLE_BG_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); 
        button.setOpaque(true); // Ensure background color is painted

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                inputManager.simulateKeyPress(keyCode);
                // Visual update is now handled by updateKeyStates, triggered externally
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                inputManager.simulateKeyRelease(keyCode);
                // Visual update is now handled by updateKeyStates, triggered externally
            }
        });
        return button;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // The main component is now transparent.
        // The background, border, and title drawing is removed.
        // The keysPanel with its buttons handles its own appearance.
    }

    public void updateKeyStates(boolean wPressed, boolean aPressed, boolean sPressed, boolean dPressed) {
        wButton.setBackground(wPressed ? KEY_ACTIVE_BG_COLOR : KEY_IDLE_BG_COLOR);
        wButton.setForeground(wPressed ? KEY_ACTIVE_FG_COLOR : TEXT_COLOR);

        aButton.setBackground(aPressed ? KEY_ACTIVE_BG_COLOR : KEY_IDLE_BG_COLOR);
        aButton.setForeground(aPressed ? KEY_ACTIVE_FG_COLOR : TEXT_COLOR);

        sButton.setBackground(sPressed ? KEY_ACTIVE_BG_COLOR : KEY_IDLE_BG_COLOR);
        sButton.setForeground(sPressed ? KEY_ACTIVE_FG_COLOR : TEXT_COLOR);

        dButton.setBackground(dPressed ? KEY_ACTIVE_BG_COLOR : KEY_IDLE_BG_COLOR);
        dButton.setForeground(dPressed ? KEY_ACTIVE_FG_COLOR : TEXT_COLOR);
    }
}
