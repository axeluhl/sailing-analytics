package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.Audio;
import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class AudioControl implements MediaPlayer {

    private static final int TOLERATED_LAG_IN_MILLISECONDS = 2000;
    private Audio audioControl;
    private MediaTrack audioTrack;

    public AudioControl() {
        audioControl = Audio.createIfSupported();
        if (audioControl != null) {
            audioControl.setControls(true);
            audioControl.setAutoplay(false);
        }
    }

    public void setMediaTrack(MediaTrack audioTrack) {
        this.audioTrack = audioTrack;
        if (this.audioTrack != null) {
            audioControl.setSrc(this.audioTrack.url);
        } else {
            audioControl.setSrc(null);
        }
    }

    public void alignTime(Date raceTime) {
        if (audioControl != null) {
            switch (audioControl.getReadyState()) {
            case MediaElement.HAVE_NOTHING:
                pause();
                break;
            default:
                long audioStartTimeInMillis = audioTrack.startTime.getTime();
                long audioTimeInMillis = audioStartTimeInMillis + Math.round(audioControl.getCurrentTime() * 1000);
                long raceTimeInMillis = raceTime.getTime();
                long videoLaggingBehindRaceInMillis = raceTimeInMillis - audioTimeInMillis;
                if (Math.abs(videoLaggingBehindRaceInMillis) > TOLERATED_LAG_IN_MILLISECONDS) {
                    double videoTime = (raceTimeInMillis - audioStartTimeInMillis) / 1000;
                    if (videoTime < 0) {
                        pause();
                    } else if (videoTime > audioControl.getDuration()) {
                        pause();
                    } else {
                        audioControl.setCurrentTime(videoTime);
                    }
                }
            }
        }
    }

    @Override
    public boolean isPaused() {
        return (audioControl == null) || audioControl.isPaused();
    }

    @Override
    public void pause() {
        if (audioControl != null) {
            audioControl.pause();
        }
    }

    @Override
    public void play() {
        if (audioControl != null) {
            audioControl.play();
        }
    }
    
}
