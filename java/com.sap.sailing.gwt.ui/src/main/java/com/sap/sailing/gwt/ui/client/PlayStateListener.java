package com.sap.sailing.gwt.ui.client;

public interface PlayStateListener {
    /**
     * Called by a timer where this listener is registered when a timer's play state has changed.
     * 
     * @param isPlaying
     *            <code>true</code> if the timer is now automatically updating its time
     */
    void playStateChanged(boolean isPlaying);
}
