package com.tavuc.ui.screens;

import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener; 
import java.awt.Component; 
import javax.swing.JComponent; 
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.tavuc.ui.panels.TitleBar;
import com.tavuc.utils.FrameControllable;


public abstract class GScreen extends JFrame implements FrameControllable {

    protected TitleBar titleBarPanel;
    protected JPanel mainPanel; 
    protected JComponent contentPanel;
    protected JLayeredPane layeredPane; 
    private boolean isActuallyFullScreen = true;
    private Timer titleBarHideTimer;
    private static final int TITLE_BAR_SHOW_THRESHOLD = 10;
    private static final int TITLE_BAR_HIDE_DELAY = 2000;

    public GScreen() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);

        layeredPane = new JLayeredPane();
        titleBarPanel = new TitleBar(this);
        
        mainPanel = new JPanel(new BorderLayout()); 
        mainPanel.setOpaque(false);
        mainPanel.add(titleBarPanel, BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout()); 
        contentPanel.setOpaque(false);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        setContentPane(layeredPane);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (mainPanel != null) {
                    mainPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                }
                for (Component comp : layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER)) {
                    comp.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                }
                layeredPane.revalidate();
                layeredPane.repaint();
            }
        });

        if (isActuallyFullScreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            titleBarPanel.setVisible(false);
        } else {
            setSize(1024, 768);
            setLocationRelativeTo(null);
            titleBarPanel.setVisible(true);
        }
        titleBarPanel.updateMaximizeButtonText(isActuallyFullScreen);

        titleBarHideTimer = new Timer(TITLE_BAR_HIDE_DELAY, e -> {
            if (isActuallyFullScreen && titleBarPanel.isVisible()) {
                Point mouseOnScreen = MouseInfo.getPointerInfo().getLocation();
                Point mouseRelativeToContentPanel = new Point(mouseOnScreen);
                SwingUtilities.convertPointFromScreen(mouseRelativeToContentPanel, contentPanel);
                if (mouseRelativeToContentPanel.y < TITLE_BAR_SHOW_THRESHOLD && mouseRelativeToContentPanel.y >= 0) {
                    titleBarHideTimer.stop();
                    return;
                }

                Point mouseRelativeToTitleBar = new Point(mouseOnScreen);
                SwingUtilities.convertPointFromScreen(mouseRelativeToTitleBar, titleBarPanel);

                if (!titleBarPanel.contains(mouseRelativeToTitleBar)) {
                    titleBarPanel.setVisible(false);
                } else {
                    titleBarHideTimer.restart();
                }
            }
        });
        titleBarHideTimer.setRepeats(false);

        MouseMotionAdapter commonMouseMotionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point pointRelativeToContentPanel;
                if (e.getSource() == titleBarPanel) {
                    pointRelativeToContentPanel = SwingUtilities.convertPoint(titleBarPanel, e.getPoint(), contentPanel);
                } else {
                    pointRelativeToContentPanel = e.getPoint();
                }
                handleMouseMovement(pointRelativeToContentPanel);
            }
        };

        if (contentPanel != null) {
            contentPanel.addMouseMotionListener(commonMouseMotionListener);
        }
        titleBarPanel.addMouseMotionListener(commonMouseMotionListener);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    toggleWindowState();
                }
            }
        });

        setFocusable(true);
    }

    protected void setScreenSpecificPanel(JComponent panel) {
        if (this.mainPanel == null) return; 

        if (this.contentPanel != null) {
            this.mainPanel.remove(this.contentPanel);
        }
        this.contentPanel = panel;
        this.mainPanel.add(this.contentPanel, BorderLayout.CENTER);

        for (MouseMotionListener l : this.contentPanel.getMouseMotionListeners()) {
            if (l.getClass().getName().contains("commonMouseMotionListener")) {
                this.contentPanel.removeMouseMotionListener(l);
            }
        }

        MouseMotionAdapter commonMouseMotionListener = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point pointRelativeToContentPanel;
                if (e.getSource() == titleBarPanel) {
                    pointRelativeToContentPanel = SwingUtilities.convertPoint(titleBarPanel, e.getPoint(), contentPanel);
                } else {
                    pointRelativeToContentPanel = e.getPoint();
                }
                handleMouseMovement(pointRelativeToContentPanel);
            }
        };
        this.contentPanel.addMouseMotionListener(commonMouseMotionListener);

        this.contentPanel.revalidate();
        this.contentPanel.repaint();
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    protected void addUILayer(JComponent uiLayer) {
        uiLayer.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
        layeredPane.add(uiLayer, JLayeredPane.PALETTE_LAYER);
        layeredPane.revalidate();
        layeredPane.repaint();
    }


    /**
     * Allows child components of contentPanel to report mouse movements,
     * ensuring GScreen's title bar logic can be triggered.
     * The point should be relative to this GScreen's contentPanel.
     * @param pointRelativeToContentPanel The mouse point relative to the contentPanel.
     */
    public void reportMouseMovedOnContent(Point pointRelativeToContentPanel) { 
        handleMouseMovement(pointRelativeToContentPanel);
    }

    private void toggleWindowState() {
        isActuallyFullScreen = !isActuallyFullScreen;

        if (isActuallyFullScreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            titleBarPanel.setVisible(false);
        } else {
            setExtendedState(JFrame.NORMAL);
            setSize(1024, 768);
            setLocationRelativeTo(null);
            titleBarPanel.setVisible(true);
        }
        titleBarPanel.updateMaximizeButtonText(isActuallyFullScreen);
    }

    private void handleMouseMovement(Point p) {
        if (contentPanel == null) return; 

        if (isActuallyFullScreen) {
            if (p.y < TITLE_BAR_SHOW_THRESHOLD && p.y >= 0 && p.x >=0 && p.x < contentPanel.getWidth()) {
                if (!titleBarPanel.isVisible()) {
                    titleBarPanel.setVisible(true);
                }
                if (titleBarHideTimer.isRunning()) {
                    titleBarHideTimer.stop();
                }
            } else {
                if (titleBarPanel.isVisible()) {
                    Point mouseOnScreen = MouseInfo.getPointerInfo().getLocation();
                    Point mouseRelativeToTitleBar = new Point(mouseOnScreen);
                    SwingUtilities.convertPointFromScreen(mouseRelativeToTitleBar, titleBarPanel);

                    if (titleBarPanel.contains(mouseRelativeToTitleBar)) {
                        if (titleBarHideTimer.isRunning()) {
                            titleBarHideTimer.stop();
                        }
                        titleBarHideTimer.restart();
                    } else {
                        titleBarPanel.setVisible(false);
                        if (titleBarHideTimer.isRunning()) {
                            titleBarHideTimer.stop();
                        }
                    }
                }
            }
        } else { 
            if (!titleBarPanel.isVisible()) {
                titleBarPanel.setVisible(true);
            }
            if (titleBarHideTimer.isRunning()) {
                titleBarHideTimer.stop();
            }
        }
    }

    @Override
    public void performMinimize() {
        setState(JFrame.ICONIFIED);
    }

    @Override
    public void performMaximizeRestore() {
        toggleWindowState();
    }

    @Override
    public void performClose() {
        System.exit(0);
    }

    @Override
    public JFrame getFrame() {
        return this;
    }

    @Override
    public abstract String getScreenTitle(); 

    @Override
    public void updateMaximizeButtonOnTitleBar(boolean isMaximized) {
        if (titleBarPanel != null) {
            titleBarPanel.updateMaximizeButtonText(isMaximized);
        }
    }

    public TitleBar getTitleBarPanel() {
        return titleBarPanel;
    }

    public boolean isActuallyFullScreen() {
        return isActuallyFullScreen;
    }
}
