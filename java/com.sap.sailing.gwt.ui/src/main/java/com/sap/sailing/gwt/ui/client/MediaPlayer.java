package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public interface MediaPlayer {

    boolean isPaused();

    void pause();

    void play();

    void alignTime(Date raceTime);

    MediaTrack getMediaTrack();
    
    void setMediaTrack(MediaTrack mediaTrack);

    void setPlaybackSpeed(double newPlaySpeedFactor);

    boolean isMuted();

    void setMuted(boolean isToBeMuted);
    
}
