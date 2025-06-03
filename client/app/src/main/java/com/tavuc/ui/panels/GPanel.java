package com.tavuc.ui.panels;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import javax.imageio.ImageIO;

public abstract class GPanel extends JPanel {

    private Image backgroundImage;

    /**
     * Default constructor for GPanel.
     * Initializes the panel with no background image.
     */
    public GPanel() {
        setOpaque(false); 
    }

    /**
     * Constructor for GPanel that sets a background image.
     * @param backgroundImagePath The path to the background image resource.
     */
    public GPanel(String backgroundImagePath) {
        this(); 
        setBackgroundImage(backgroundImagePath);
    }

    /**
     * Sets the background image for the panel.
     * @param imagePath The path to the background image resource.
     */
    protected void setBackgroundImage(String imagePath) {
        try {
            backgroundImage = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException e) {
            System.err.println("Failed to load background image: " + imagePath + " - " + e.getMessage());
            backgroundImage = null;
        }
    }

    /**
     * Paints the component with the background image.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
        }
    }

}
