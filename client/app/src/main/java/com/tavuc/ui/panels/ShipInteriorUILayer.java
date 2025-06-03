package com.tavuc.ui.panels;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JButton;

public class ShipInteriorUILayer extends GPanel {

    private JButton exitButton; 

    public ShipInteriorUILayer() {
        super();
        setOpaque(false); 
        setLayout(null); 
        //TODO: ADD EXIT SHIP BUTTON

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }


}
