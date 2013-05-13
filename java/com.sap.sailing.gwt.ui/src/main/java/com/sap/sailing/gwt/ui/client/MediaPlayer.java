package com.sap.sailing.gwt.ui.client;

import java.util.Date;

import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack;

public interface MediaPlayer {

    boolean isPaused();

    void pause();

    void play();

    void alignTime(Date raceTime);

    MediaTrack getMediaTrack();
    
    double getDuration();
    
    void setTime(double mediaTime);
    
    double getTime();
    
    void setPlaybackSpeed(double newPlaySpeedFactor);

    void setMuted(boolean isToBeMuted);

    void destroy();

}
