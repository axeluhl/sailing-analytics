package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public abstract class AbstractMediaPlayer implements MediaPlayer {

    private static final int TOLERATED_LAG_IN_MILLISECONDS = 2000;
    private final MediaTrack mediaTrack;

    protected AbstractMediaPlayer(MediaTrack mediaTrack) {
        this.mediaTrack = mediaTrack;
    }

    public MediaTrack getMediaTrack() {
        return mediaTrack;
    }

    public void alignTime(Date raceTime) {
        long mediaStartTimeInMillis = mediaTrack.startTime.getTime();
        long mediaTimeInMillis = mediaStartTimeInMillis + Math.round(getTime() * 1000);
        long raceTimeInMillis = raceTime.getTime();
        long mediaLaggingBehindRaceInMillis = raceTimeInMillis - mediaTimeInMillis;
        if (Math.abs(mediaLaggingBehindRaceInMillis) > TOLERATED_LAG_IN_MILLISECONDS) {
            double mediaTime = (raceTimeInMillis - mediaStartTimeInMillis) / 1000;
            if (mediaTime < 0) {
                pause();
            } else if (mediaTime > getDuration()) {
                pause();
            } else {
                setTime(mediaTime);
            }
        }
    }

}
