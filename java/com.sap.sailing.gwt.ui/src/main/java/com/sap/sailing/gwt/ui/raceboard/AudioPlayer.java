package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.dom.client.AudioElement;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.Audio;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class AudioPlayer extends AbstractMediaPlayer {
    
    /**
     * isReady and the ..Cache fields are required because setting the audio element's play state 
     * needs to be deferred until the media's metadata has been loaded.
     */
    private boolean isReady = false;
    private double deferredMediaTime;
    private double deferredPlaybackSpeed = 1;
    private boolean isDeferredPlaying;
    private boolean isDeferredMuted;
    
    private final Audio audio;

    public AudioPlayer(MediaTrack mediaTrack) {
        super(mediaTrack);
        audio = Audio.createIfSupported();
        if (audio != null) {
            audio.setControls(false);
            audio.setAutoplay(false);
            audio.setLoop(false);
            audio.setMuted(false);
            audio.setPreload(MediaElement.PRELOAD_AUTO);
            addNativeEventHandlers(audio.getAudioElement());
            audio.setSrc(mediaTrack.url);
            audio.setTitle(mediaTrack.title);
        }
    }
    
    native void addNativeEventHandlers(AudioElement audioElement) /*-{
    var that = this;
    audioElement.addEventListener('canplay',
                                    function() {
                                            that.@com.sap.sailing.gwt.ui.raceboard.AudioPlayer::initPlayState()();
                                    });
    }-*/;
    
    private void initPlayState() {
        if (!isReady && (audio != null)) {
            audio.setCurrentTime(deferredMediaTime);
            audio.setMuted(isDeferredMuted);
            audio.setPlaybackRate(deferredPlaybackSpeed);
            if (isDeferredPlaying) {
                audio.play();
            } else {
                audio.pause();
            }
        }
        isReady = true;
    }

    @Override
    public boolean isPaused() {
        if (isReady) {
            return audio.isPaused();
        } else {
            return !isDeferredPlaying;
        }
    }

    @Override
    public void pause() {
        isDeferredPlaying = false;
        if (isReady && audio != null) {
            audio.pause();
        }
    }

    @Override
    public void play() {
        isDeferredPlaying = true;
        if (isReady && audio != null) {
            audio.play();
        }
    }

    @Override
    public void setPlaybackSpeed(double playbackSpeed) {
        this.deferredPlaybackSpeed = playbackSpeed; 
        if (isReady && audio != null) {
            audio.setPlaybackRate(playbackSpeed);
        }
    }
    
    @Override
    public void setMuted(boolean isToBeMuted) {
        isDeferredMuted = isToBeMuted;
        if (isReady && audio != null) {
            audio.setMuted(isToBeMuted);
        }
    }

    @Override
    public double getDuration() {
        if (isReady) {
            return audio.getDuration();
        } else {
            return Double.NaN;
        }
    }

    @Override
    public double getTime() {
        if (isReady) {
            return audio.getCurrentTime();
        } else {
            return Double.NaN;
        }
    }
    
    @Override
    public void setTime(double mediaTime) {
        this.deferredMediaTime = mediaTime;
        if (isReady && audio != null) {
            audio.setCurrentTime(mediaTime);
        }
    }

    @Override
    public void destroy() {
        audio.pause();
        audio.setSrc(null);
    }

}
