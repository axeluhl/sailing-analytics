package com.sap.sse.gwt.client.player;

import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;

public interface PlayStateListener {
    /**
     * Called by a timer where this listener is registered when a timer's play state has changed.
     * 
     * @param isPlaying
     *            <code>true</code> if the timer is now automatically updating its time
     */
    void playStateChanged(PlayStates playState, PlayModes playMode);
    
    void playSpeedFactorChanged(double newPlaySpeedFactor);
}       
