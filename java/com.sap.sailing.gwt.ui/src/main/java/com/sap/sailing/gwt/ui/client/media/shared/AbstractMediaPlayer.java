package com.sap.sailing.gwt.ui.client.media.shared;

import java.util.Date;

import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;


public abstract class AbstractMediaPlayer implements MediaPlayer {

    private static final int TOLERATED_LAG_IN_MILLISECONDS = 4000;   
    private final MediaTrackWithSecurityDTO mediaTrack;
    private long raceTimeInMillis;

    protected AbstractMediaPlayer(MediaTrackWithSecurityDTO mediaTrack) {
        this.mediaTrack = mediaTrack;
    }

    public MediaTrackWithSecurityDTO getMediaTrack() {
        return mediaTrack;
    }
    
    protected void onMediaTimeUpdate() {
        //default no op
    }

    public void forceAlign() {
        forceAlign(mediaTrack.startTime.asMillis());
    }

    public void raceTimeChanged(Date raceTime) {
        if (raceTime != null) {
            raceTimeInMillis = raceTime.getTime();
            alignTime();
        }
    }
    
    @Override
    public long getCurrentMediaTimeMillis() {
        return Math.round(getCurrentMediaTime() * 1000);
    }
    
    @Override
	public boolean isCoveringCurrentRaceTime() {
        double mediaTime = (raceTimeInMillis - mediaTrack.startTime.asMillis()) / 1000d;
        double duration = getDuration();
        return (mediaTime >= 0) && ((duration == 0) || (mediaTime <= duration)); // for unknown reasons, Youtube player sometimes returns duration = 0 for live stream
	}

    protected void alignTime() {
        long mediaStartTimeInMillis = mediaTrack.startTime.asMillis();
        long mediaTimeInMillis = mediaStartTimeInMillis + getCurrentMediaTimeMillis();
        long mediaTimeOffFromRaceInMillis = raceTimeInMillis - mediaTimeInMillis;
        if (isOutOfTolerance(mediaTimeOffFromRaceInMillis)) {
            forceAlign(mediaStartTimeInMillis);
        }
    }

    private boolean isOutOfTolerance(long mediaTimeOffFromRaceInMillis) {
        return Math.abs(mediaTimeOffFromRaceInMillis) > TOLERATED_LAG_IN_MILLISECONDS;
    }

    private void forceAlign(long mediaStartTimeInMillis) {
        double mediaTime = (raceTimeInMillis - mediaStartTimeInMillis) / 1000d;
        if (mediaTime < 0) {
            pauseMedia();
        } else {
            double duration = getDuration();
            double diff = mediaTime - duration;
            if (duration == 0 || diff <= 0) {
                setCurrentMediaTime(mediaTime);
            } else {
                pauseMedia();
            }
        }
    }

}
