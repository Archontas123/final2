package com.tavuc.ui.panels;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.BorderLayout;

public class GameScreenUILayer extends JPanel {

    private JButton actionButton;

    public GameScreenUILayer() {
        setOpaque(false);
        setFocusable(false);
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);

        actionButton = new JButton("Return to Ship");
        actionButton.setFont(new Font("Consolas", Font.BOLD, 14)); 
        actionButton.setBackground(new Color(45, 60, 80)); 
        actionButton.setForeground(new Color(220, 220, 220)); 
        actionButton.setFocusPainted(false);
        actionButton.setFocusable(false); 
        actionButton.setPreferredSize(new Dimension(180, 45)); 
        actionButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 100, 120), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        buttonPanel.add(actionButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public JButton getActionButton() {
        return actionButton;
    }
}
