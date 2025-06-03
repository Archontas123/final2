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

public class SpaceScreenUILayer extends JPanel { 

    private JButton actionButton;
    private JLabel coordinatesLabel;

    public SpaceScreenUILayer() { 
        setOpaque(false);
        setFocusable(false);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setOpaque(false);
        coordinatesLabel = new JLabel("X: 0, Y: 0");
        coordinatesLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        coordinatesLabel.setForeground(new Color(220, 220, 220));
        topPanel.add(coordinatesLabel);
        add(topPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);

        actionButton = new JButton("Enter Ship Interior");
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

    public void updateCoordinates(double x, double y) {
        coordinatesLabel.setText(String.format("X: %.2f, Y: %.2f", x, y));
    }
}
