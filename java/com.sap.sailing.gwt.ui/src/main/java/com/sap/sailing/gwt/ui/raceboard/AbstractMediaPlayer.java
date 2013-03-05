package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public abstract class AbstractMediaPlayer implements MediaPlayer {

    private static final int TOLERATED_LAG_IN_MILLISECONDS = 2000;
    private final MediaTrack mediaTrack;
    private long raceTimeInMillis;
    private final MediaEventHandler mediaEventHandler;

    protected AbstractMediaPlayer(MediaTrack mediaTrack, MediaEventHandler mediaEventHandler) {
        this.mediaTrack = mediaTrack;
        this.mediaEventHandler = mediaEventHandler;
    }

    public MediaTrack getMediaTrack() {
        return mediaTrack;
    }
    
    protected void onMediaTimeUpdate() {
        pause();
        mediaEventHandler.timeUpdate();
    }

    public void forceAlign() {
        forceAlign(mediaTrack.startTime.getTime());
    }

    public void alignTime(Date raceTime) {
        raceTimeInMillis = raceTime.getTime();
        long mediaStartTimeInMillis = mediaTrack.startTime.getTime();
        long mediaTimeInMillis = mediaStartTimeInMillis + Math.round(getCurrentMediaTime() * 1000);
        long mediaLaggingBehindRaceInMillis = raceTimeInMillis - mediaTimeInMillis;
        if (Math.abs(mediaLaggingBehindRaceInMillis) > TOLERATED_LAG_IN_MILLISECONDS) {
            forceAlign(mediaStartTimeInMillis);
        }
    }

    private void forceAlign(long mediaStartTimeInMillis) {
        double mediaTime = (raceTimeInMillis - mediaStartTimeInMillis) / 1000d;
        if (mediaTime < 0) {
            pause();
        } else if (mediaTime > getDuration()) {
            pause();
        } else {
            setCurrentMediaTime(mediaTime);
        }
    }

}
