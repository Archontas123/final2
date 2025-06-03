package com.tavuc.utils;

import javax.swing.JFrame;

public interface FrameControllable {

    /**
     * Performs the minimize action on the frame.
     */
    void performMinimize();
    /**
     * Performs the maximize or restore action on the frame.
     */
    void performMaximizeRestore();
    /**
     * Performs the close action on the frame.
     */
    void performClose();
    /**
     * Returns the current state of the frame.
     * @return true if the frame is maximized, false otherwise
     */
    JFrame getFrame(); 
    /**
     * Returns the title of the screen.
     * @return the title of the screen
     */
    String getScreenTitle(); 
    /**
     * Updates the maximize button on the title bar based on the current state of the frame.
     * @param isMaximized true if the frame is maximized, false otherwise
     */
    void updateMaximizeButtonOnTitleBar(boolean isMaximized); 
}
