package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.MediaBase;
import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public abstract class AbstractMediaPlayer implements MediaPlayer {

    private static final int TOLERATED_LAG_IN_MILLISECONDS = 2000;
    protected final MediaBase mediaControl;
    private MediaTrack mediaTrack;

    public AbstractMediaPlayer(MediaBase mediaControl) {
        this.mediaControl = mediaControl;
        if (mediaControl != null) {
            mediaControl.setControls(false);
            mediaControl.setAutoplay(false);
            mediaControl.setLoop(false);
            mediaControl.setPreload(MediaElement.PRELOAD_AUTO);
        }
}

    public MediaTrack getMediaTrack() {
        return mediaTrack;
    }

    public void setMediaTrack(MediaTrack mediaTrack) {
        this.mediaTrack = mediaTrack;
        if (this.mediaTrack != null) {
            mediaControl.setSrc(this.mediaTrack.url);
            mediaControl.setTitle(mediaTrack.title);
        } else {
            mediaControl.setSrc(null);
            mediaControl.setTitle(null);
        }
    }

    public void alignTime(Date raceTime) {
        switch (mediaControl.getReadyState()) {
        case MediaElement.HAVE_NOTHING:
            pause();
            break;
        default:
            long mediaStartTimeInMillis = mediaTrack.startTime.getTime();
            long mediaTimeInMillis = mediaStartTimeInMillis + Math.round(mediaControl.getCurrentTime() * 1000);
            long raceTimeInMillis = raceTime.getTime();
            long mediaLaggingBehindRaceInMillis = raceTimeInMillis - mediaTimeInMillis;
            if (Math.abs(mediaLaggingBehindRaceInMillis) > TOLERATED_LAG_IN_MILLISECONDS) {
                double mediaTime = (raceTimeInMillis - mediaStartTimeInMillis) / 1000;
                if (mediaTime < 0) {
                    pause();
                } else if (mediaTime > mediaControl.getDuration()) {
                    pause();
                } else {
                    mediaControl.setCurrentTime(mediaTime);
                }
            }
        }
    }

    @Override
    public boolean isPaused() {
        return (mediaControl == null) || mediaControl.isPaused();
    }

    @Override
    public void pause() {
        if (mediaControl != null) {
            mediaControl.pause();
        }
    }

    @Override
    public void play() {
        if (mediaControl != null) {
            mediaControl.play();
        }
    }

    @Override
    public void setPlaybackSpeed(double playbackSpeed) {
        mediaControl.setPlaybackRate(playbackSpeed);
    }
    
    @Override
    public boolean isMuted() {
        return mediaControl.isMuted();
    }

    @Override
    public void setMuted(boolean isToBeMuted) {
        mediaControl.setMuted(isToBeMuted);
    }

}
