package com.tavuc.ui.dialog;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.tavuc.Client;
import com.tavuc.ui.panels.GradientDialogPanel;
import com.tavuc.ui.screens.StartScreen;

import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tavuc.networking.models.LoginResponse;
import com.tavuc.networking.models.RegisterResponse;


public class LoginDialog extends JDialog {

    private static final Gson gson = new Gson();
    private JTextField usernameField;
    private JPasswordField passwordField; 
    private JButton loginButton;
    private JFrame parent;
    private boolean registering = false;

    /**
     * Constructor for LoginDialog
     * Initializes the dialog with a parent frame and sets up the UI components.
     * @param parent The parent JFrame for this dialog.
     */
    public LoginDialog(JFrame parent) {
        super(parent, "Login", true); 
        this.parent = parent;
        GradientDialogPanel panel = new GradientDialogPanel(new Color(55, 55, 55, 230));
        panel.setLayout(new GridBagLayout()); 

        setUndecorated(true);
        
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 2), 
            BorderFactory.createEmptyBorder(20, 20, 20, 20) 
        ));

        usernameField = new JTextField("Username");
        usernameField.setPreferredSize(new Dimension(250, 40)); 

        passwordField = new JPasswordField(); 
        passwordField.setPreferredSize(new Dimension(250, 40));
        
        loginButton = new JButton("Login");

        StartScreen.styleTextField(usernameField);
    
        JTextField tempPasswordFieldForPlaceholder = new JTextField("Password");
        StartScreen.styleTextField(tempPasswordFieldForPlaceholder); 
        
        passwordField.setEchoChar((char) 0); 
        passwordField.setText("Password");
        passwordField.setFont(tempPasswordFieldForPlaceholder.getFont());
        passwordField.setForeground(tempPasswordFieldForPlaceholder.getForeground());
        
        passwordField.setBackground(tempPasswordFieldForPlaceholder.getBackground());
        passwordField.setBorder(tempPasswordFieldForPlaceholder.getBorder());
        passwordField.setCaretColor(tempPasswordFieldForPlaceholder.getCaretColor());

        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).equals("Password")) {
                    passwordField.setText("");
                    passwordField.setEchoChar('\u2022');
                
                    JTextField tempInputStyleField = new JTextField();
                    StartScreen.styleTextField(tempInputStyleField); 
                    passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
                    passwordField.setForeground(new Color(255, 240, 170)); 
                }
            }
            public void focusLost(FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).isEmpty()) {
                    passwordField.setEchoChar((char)0); 
                    passwordField.setText("Password");
                    passwordField.setFont(new Font("Segoe UI", Font.ITALIC, 14)); 
                    passwordField.setForeground(new Color(150, 150, 150)); 
                }
            }
        });


        StartScreen.styleButton(loginButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 20, 15, 20); 

        panel.add(usernameField, gbc);
        panel.add(passwordField, gbc);
        
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(25, 0, 10, 0); 
        panel.add(loginButton, gbc);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = usernameField.getText();
                String pass = new String(passwordField.getPassword());

                System.out.println("Attempting action for user: " + user + (registering ? " (Registering)" : " (Logging in)"));

                if (user.isEmpty() || pass.isEmpty() || user.equals("Username") || pass.equals("Password")) {
                    JOptionPane.showMessageDialog(LoginDialog.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (registering) {
                    try {
                        String jsonRegistrationResponse = Client.register(user, pass);
                        System.out.println("Server registration response: " + jsonRegistrationResponse);
                        RegisterResponse regResponse = null;
                        try {
                            regResponse = gson.fromJson(jsonRegistrationResponse, RegisterResponse.class);
                        } catch (JsonSyntaxException jsonEx) {
                            JOptionPane.showMessageDialog(LoginDialog.this, "Registration failed: Invalid response format from server.", "Error", JOptionPane.ERROR_MESSAGE);
                            jsonEx.printStackTrace();
                            return;
                        }

                        if (regResponse != null && regResponse.success) {
                            JOptionPane.showMessageDialog(LoginDialog.this, "Registration successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            if (parent instanceof StartScreen) {
                            }
                            dispose();
                        } else {
                            String msg = (regResponse != null && regResponse.message != null) ? regResponse.message : "Unknown registration error.";
                            JOptionPane.showMessageDialog(LoginDialog.this, "Registration failed: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        String errorMessage = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                        JOptionPane.showMessageDialog(LoginDialog.this, "Registration error: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        String jsonLoginResponse = Client.login(user, pass);
                        System.out.println("Server login response: " + jsonLoginResponse);
                        LoginResponse loginResp = null;
                        
                        if (jsonLoginResponse == null) {
                             JOptionPane.showMessageDialog(LoginDialog.this, "Login failed: No response from server.", "Error", JOptionPane.ERROR_MESSAGE);
                             return;
                        }

                        try {
                            loginResp = gson.fromJson(jsonLoginResponse, LoginResponse.class);
                        } catch (JsonSyntaxException jsonEx) {
                            JOptionPane.showMessageDialog(LoginDialog.this, "Login failed: Invalid response format from server.", "Error", JOptionPane.ERROR_MESSAGE);
                            jsonEx.printStackTrace();
                            return;
                        }
                        
                        if (loginResp != null && loginResp.success) {
                            JOptionPane.showMessageDialog(LoginDialog.this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            if (parent instanceof StartScreen) {
                                ((StartScreen) parent).getStartScreenPanel().updateLoginState();
                            }
                            dispose();
                        } else if (loginResp != null && loginResp.message != null && loginResp.message.contains("Player not found")) {
                            int choice = JOptionPane.showConfirmDialog(LoginDialog.this, "Player not found. Would you like to register this new player?", "Player Not Found", JOptionPane.YES_NO_OPTION);
                            if (choice == JOptionPane.YES_OPTION) {
                                registering = true;
                                setTitle("Register");
                                loginButton.setText("Register");
                                StartScreen.styleButton(loginButton);
                            }
                        } else {
                            String msg = (loginResp != null && loginResp.message != null) ? loginResp.message : "Username or password incorrect.";
                            JOptionPane.showMessageDialog(LoginDialog.this, "Login failed: " + msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        String errorMessage = ex.getMessage() == null ? ex.toString() : ex.getMessage();
                        JOptionPane.showMessageDialog(LoginDialog.this, "Login error: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });
        
        setContentPane(panel);
        pack(); 
        setLocationRelativeTo(parent); 
    }
}
