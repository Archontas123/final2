package com.tavuc.ui.panels;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel; 
import javax.swing.SwingUtilities;

import com.tavuc.Client;
import com.tavuc.ui.screens.StartScreen;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Insets;



public class StartScreenPanel extends GPanel {

    private JLabel startGameLabel;
    private JLabel loginLabel;
    private JLabel settingsLabel;
    private StartScreen startScreenFrame;
    private boolean loggedIn = false;
    private static final Color ACTIVE_LABEL_COLOR = Color.WHITE;
    private static final Color HOVER_LABEL_COLOR = Color.YELLOW;
    private static final Color DISABLED_LABEL_COLOR = Color.GRAY;

    /**
     * Constructor for StartScreenPanel
     * Initializes the panel with the start screen frame and loads the background image.
     * @param startScreenFrame The parent StartScreen frame.
     */
    public StartScreenPanel(StartScreen startScreenFrame) {
        super("/images/background.png"); 
        this.startScreenFrame = startScreenFrame;

        setLayout(new GridBagLayout());

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        
        Font labelFont = new Font("Verdana", Font.BOLD, 24); 

        startGameLabel = new JLabel("Start Game");
        loginLabel = new JLabel("Login");
        settingsLabel = new JLabel("Settings");

        configureLabel(startGameLabel, labelFont, loggedIn ? ACTIVE_LABEL_COLOR : DISABLED_LABEL_COLOR, HOVER_LABEL_COLOR, DISABLED_LABEL_COLOR, () -> {
            if (loggedIn) {
                System.out.println("Start Game clicked");
                startScreenFrame.startGame();
            }
        });

        configureLabel(loginLabel, labelFont, ACTIVE_LABEL_COLOR, HOVER_LABEL_COLOR, DISABLED_LABEL_COLOR, () -> { 
            if (!loggedIn) { 
                System.out.println("Login clicked");
                startScreenFrame.showLoginDialog();
            }
        });

        configureLabel(settingsLabel, labelFont, ACTIVE_LABEL_COLOR, HOVER_LABEL_COLOR, null, () -> {
            System.out.println("Settings clicked");
        });
        
        menuPanel.add(startGameLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
        menuPanel.add(loginLabel);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
        menuPanel.add(settingsLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(0, 50, 50, 0); 
        add(menuPanel, gbc);

        updateLoginState(); 
    }

    /**
     * Configures a JLabel with the specified properties and mouse listeners.
     * @param label The JLabel to configure.
     * @param font The font to set for the label.
     * @param defaultColor The default foreground color of the label.
     * @param hoverColor The foreground color when the mouse hovers over the label.
     * @param disabledColor The foreground color when the label is disabled.
     * @param onClickAction The action to perform when the label is clicked.
     */
    private void configureLabel(JLabel label, Font font, Color defaultColor, Color hoverColor, Color disabledColor, Runnable onClickAction) {
        label.setFont(font);
        label.setForeground(defaultColor);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setAlignmentX(Component.LEFT_ALIGNMENT); 
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (label == loginLabel) {
                    if (!loggedIn) {
                        onClickAction.run();
                    }
                } else { 
                    if (label.getForeground() != disabledColor) {
                        onClickAction.run();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (label == loginLabel) {
                    if (!loggedIn) {
                        label.setForeground(hoverColor);
                    }
                } else { 
                    if (label.getForeground() != disabledColor) {
                        label.setForeground(hoverColor);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (label.getForeground() == hoverColor) {
                    if (label == startGameLabel) {
                        label.setForeground(loggedIn ? ACTIVE_LABEL_COLOR : DISABLED_LABEL_COLOR);
                    } else if (label == loginLabel) {
                        label.setForeground(loggedIn ? DISABLED_LABEL_COLOR : ACTIVE_LABEL_COLOR);
                    } else { 
                        label.setForeground(defaultColor);
                    }
                }
            }
        });
    }

    /**
     * Updates the login state of the panel and adjusts the label colors and cursors accordingly.
     * This method should be called whenever the login state changes.
     */
    public void updateLoginState() {
        Client client = Client.getInstance();
        loggedIn = client != null && client.isLoggedIn();
        
        SwingUtilities.invokeLater(() -> {
            if (loggedIn) {
                startGameLabel.setForeground(ACTIVE_LABEL_COLOR);
                startGameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

                loginLabel.setForeground(DISABLED_LABEL_COLOR);
                loginLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                startGameLabel.setForeground(DISABLED_LABEL_COLOR);
                startGameLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                loginLabel.setForeground(ACTIVE_LABEL_COLOR);
                loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            revalidate();
            repaint();
        });
    }


 
}
