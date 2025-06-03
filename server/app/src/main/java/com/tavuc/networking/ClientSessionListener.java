package com.tavuc.networking;

public interface ClientSessionListener {
    
    /**
     * Called when a client session is closed.
     * @param session the sesson that was closed
     */
    void onSessionClosed(ClientSession session);
}
