package com.tavuc.ui.panels;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GradientDialogPanel extends JPanel {

    private Color backgroundColor;

    /**
     * Constructor for GradientDialogPanel
     * @param backgroundColor The solid color to be used as the background.
     */
    public GradientDialogPanel(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        setOpaque(false); 
    }

    /**
     * Override the paintComponent method to draw the solid background color.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        g2d.dispose();
    }
}
