package com.sap.sailing.gwt.ui.client.media.shared;

import java.util.Date;

import com.sap.sailing.domain.common.media.MediaTrack;


public interface MediaPlayer {

    boolean isMediaPaused();

    void pauseMedia();

    void playMedia();

    void raceTimeChanged(Date raceTime);

    MediaTrack getMediaTrack();
    
    double getDuration();
    
    double getCurrentMediaTime();
    
    void setCurrentMediaTime(double mediaTime);
    
    long getCurrentMediaTimeMillis();
    
    void setPlaybackSpeed(double newPlaySpeedFactor);

    void setMuted(boolean isToBeMuted);

    void shutDown();

	boolean isCoveringCurrentRaceTime();

}
