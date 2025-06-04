package com.tavuc.ui.components;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.FlowLayout; // Kept for textContainer, though might simplify
import java.awt.geom.RoundRectangle2D;

public class DialogComponent extends JPanel {
    private JTextArea dialogTextArea;
    private JPanel iconPanel; // This will be custom painted

    // Styled colors
    // COMPONENT_BG_COLOR is removed as the main container will be transparent
    private static final Color BORDER_COLOR = new Color(70, 75, 85);
    private static final Color ACCENT_COLOR = new Color(120, 170, 190); // Kept if needed for other accents, though title is gone
    private static final Color TEXT_COLOR = new Color(200, 200, 210);
    private static final Color ICON_PLACEHOLDER_BG_COLOR = new Color(45, 50, 65);
    private static final Color DIALOG_TEXT_AREA_BG_COLOR = new Color(25, 30, 40, 220); // Semi-transparent

    private static final Font UI_FONT_BOLD = new Font("Serif", Font.BOLD, 16); // Title
    private static final Font DIALOG_FONT = new Font("Serif", Font.PLAIN, 14); // Dialog text
    private static final Font ICON_PLACEHOLDER_FONT = new Font("Serif", Font.BOLD, 48);


    private static final int ICON_AREA_SIZE = 80;
    private static final int DEFAULT_DIALOG_WIDTH = 300; // Width of the text area part
    private static final int DEFAULT_DIALOG_HEIGHT = 100; // Preferred height of the text area part
    private static final int PADDING = 10;
    private static final int TITLE_AREA_HEIGHT = 30;
    private static final int BORDER_THICKNESS = 2;
    private static final int ROUND_CORNER_ARC = 10;
    private static final int HORIZONTAL_GAP = 10; // Gap between icon and text area

    public DialogComponent() {
        setFocusable(false);
        setLayout(new BorderLayout(0,0)); // Main panel uses BorderLayout
        setOpaque(false); // Main component is transparent

        // Calculate preferred size based on content only (icon + text_area + gaps + padding around them)
        int contentWidth = ICON_AREA_SIZE + HORIZONTAL_GAP + DEFAULT_DIALOG_WIDTH;
        int contentHeight = Math.max(ICON_AREA_SIZE, DEFAULT_DIALOG_HEIGHT);
        // The PADDING will now be for the contentPanel itself, making the DialogComponent tightly wrap contentPanel.
        setPreferredSize(new Dimension(contentWidth + 2 * PADDING, contentHeight + 2 * PADDING));


        iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(ICON_PLACEHOLDER_BG_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), ROUND_CORNER_ARC / 2f, ROUND_CORNER_ARC / 2f));
                g2d.setColor(BORDER_COLOR);
                g2d.draw(new RoundRectangle2D.Float(0, 0, getWidth() -1 , getHeight() -1, ROUND_CORNER_ARC / 2f, ROUND_CORNER_ARC / 2f));

                g2d.setFont(ICON_PLACEHOLDER_FONT);
                g2d.setColor(BORDER_COLOR); // Darker color for placeholder
                String placeholder = "?";
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int stringX = (getWidth() - fm.stringWidth(placeholder)) / 2;
                int stringY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(placeholder, stringX, stringY);
                g2d.dispose();
            }
        };
        iconPanel.setPreferredSize(new Dimension(ICON_AREA_SIZE, ICON_AREA_SIZE));
        iconPanel.setOpaque(false); 
        iconPanel.setFocusable(false);

        dialogTextArea = new JTextArea("Dialog text will appear here...");
        dialogTextArea.setEditable(false);
        dialogTextArea.setFocusable(false);
        dialogTextArea.setLineWrap(true);
        dialogTextArea.setWrapStyleWord(true);
        dialogTextArea.setFont(DIALOG_FONT);
        dialogTextArea.setForeground(TEXT_COLOR);
        dialogTextArea.setBackground(DIALOG_TEXT_AREA_BG_COLOR); 
        dialogTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1), 
            BorderFactory.createEmptyBorder(5, 5, 5, 5)    
        ));
        // Preferred size for text area is tricky with dynamic text, set a base
        dialogTextArea.setPreferredSize(new Dimension(DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT));


        // Container for icon and text area
        JPanel contentPanel = new JPanel(new BorderLayout(HORIZONTAL_GAP, 0));
        contentPanel.setOpaque(false);
        // Border now only provides padding around the content, no extra space for title
        contentPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        contentPanel.add(iconPanel, BorderLayout.WEST);
        contentPanel.add(dialogTextArea, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // The main component is now transparent.
        // The background, border, and title drawing is removed.
        // iconPanel and dialogTextArea handle their own backgrounds.
    }

    public void setDialogText(String text) {
        dialogTextArea.setText(text);
        // Potentially revalidate/repaint if text causes size changes, though JTextArea handles some of this.
    }

    public void setIcon(/* Some Icon representation, e.g., Image */) {
        // Logic to update the iconPanel's appearance
        // This would involve changing what's drawn in iconPanel.paintComponent
        // For example, storing an Image and drawing it instead of the placeholder.
        iconPanel.repaint();
    }
}
