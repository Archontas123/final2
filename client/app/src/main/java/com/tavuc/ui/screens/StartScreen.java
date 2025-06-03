package com.tavuc.ui.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.tavuc.Client;
import com.tavuc.ui.dialog.LoginDialog;
import com.tavuc.ui.panels.StartScreenPanel;
import com.tavuc.ui.panels.TitleBar;

import javax.swing.JOptionPane;



public class StartScreen extends GScreen {

    private LoginDialog loginDialog;
    private StartScreenPanel startScreenPanel;

    public StartScreen() {
        super();
        startScreenPanel = new StartScreenPanel(this);
        setScreenSpecificPanel(startScreenPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (loginDialog != null && loginDialog.isVisible()) {
                    loginDialog.dispose();
                }
            }
        });
        setVisible(true);
        requestFocusInWindow();
    }

    @Override
    public String getScreenTitle() {
        return "Game Launcher";
    }

    @Override
    public void performClose() {
        if (loginDialog != null && loginDialog.isVisible()) {
            loginDialog.dispose();
        }
        super.performClose(); 
    }


    /**
     * Displays the login dialog for user authentication.
     * If the dialog is already visible, it will not create a new one.
     */
    public void showLoginDialog() {
        if (loginDialog == null) {
            loginDialog = new LoginDialog(this);
        }
        loginDialog.setVisible(true);
    }

    /**
     * Starts the game by navigating to the Space Screen.
     * This method checks if the user is logged in and then opens the Space Screen.
     * If not logged in, it shows a warning message.
     */
    public void startGame() {
        System.out.println("Navigating to Space Screen...");
        Client client = Client.getInstance();
        if (client != null && client.isLoggedIn()) {
            this.dispose(); 
            if (loginDialog != null && loginDialog.isVisible()) {
                loginDialog.dispose();
            }
            int playerId = client.getPlayerId();
            String username = client.getUsername();
            SwingUtilities.invokeLater(() -> {
                SpaceScreen spaceScreen = new SpaceScreen(this, playerId, username);
                spaceScreen.setVisible(true);
            });
            System.out.println("Space Screen opened!");
        } else {
            JOptionPane.showMessageDialog(this, "You must be logged in to proceed.", "Login Required", JOptionPane.WARNING_MESSAGE);
            System.out.println("Login required to proceed to space screen.");
        }
    }

    /**
     * Gets the StartScreenPanel associated with this StartScreen.
     * @return the StartScreenPanel instance
     */
    public StartScreenPanel getStartScreenPanel() {
        return startScreenPanel;
    }


    /**
     * Styles a JButton with a custom background, foreground, and border color.
     * @param button the JButton to style
     */
    public static void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        Color btnBgColor = new Color(60, 60, 60);
        Color btnFgColor = new Color(255, 215, 0);
        Color btnBorderColor = new Color(255, 215, 0);

        String originalText = button.getText();

        String bgHex = String.format("%06x", btnBgColor.getRGB() & 0xFFFFFF);
        String fgHex = String.format("%06x", btnFgColor.getRGB() & 0xFFFFFF);
        String borderHex = String.format("%06x", btnBorderColor.getRGB() & 0xFFFFFF);

        button.setText("<html><div style='" +
                "padding: 10px 20px; " +
                "border: 2px solid #" + borderHex + "; " +
                "background-color: #" + bgHex + "; " +
                "color: #" + fgHex + "; " +
                "font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; " +
                "font-weight: bold; " +
                "font-size: 12pt;" +
                "text-align: center;" +
                "border-radius: 8px; " +
                "'>" + originalText + "</div></html>");
    }

    public TitleBar getTitleBarPanel() { 
        return titleBarPanel; 
    }

    /**
     * Styles a JTextField with a custom background, foreground, and border color.
     * @param textField the JTextField to style
     */
    public static void styleTextField(javax.swing.JTextField textField) {
        Color fieldBgColor = new Color(45, 45, 45);
        Color inputFgColor = new Color(255, 240, 170);
        Color placeholderColor = new Color(150, 150, 150);
        Color fieldBorderColor = new Color(100, 100, 100);

        textField.setBackground(fieldBgColor);
        textField.setForeground(inputFgColor);
        textField.setCaretColor(new Color(255, 215, 0));
        
        final Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);
        final Font placeholderFont = new Font("Segoe UI", Font.ITALIC, 14);
        
     
        textField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(fieldBorderColor, 2),
            javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        final String placeholderText = textField.getText();

        textField.setFont(placeholderFont);
        textField.setForeground(placeholderColor);

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholderText) && textField.getFont().isItalic()) {
                    textField.setText("");
                    textField.setFont(inputFont);
                    textField.setForeground(inputFgColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setFont(placeholderFont);
                    textField.setForeground(placeholderColor);
                    textField.setText(placeholderText); 
                }
            }
        });
    }
}
