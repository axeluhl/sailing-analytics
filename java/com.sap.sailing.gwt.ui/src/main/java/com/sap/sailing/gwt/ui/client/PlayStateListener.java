package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;

public interface PlayStateListener {
    /**
     * Called by a timer where this listener is registered when a timer's play state has changed.
     * 
     * @param isPlaying
     *            <code>true</code> if the timer is now automatically updating its time
     */
    void playStateChanged(PlayStates playState, PlayModes playMode);
}       
