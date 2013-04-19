package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.MediaBase;
import com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer;
import com.sap.sailing.gwt.ui.client.shared.media.MediaTrack;

abstract public class AbstractEmbeddedMediaPlayer extends AbstractMediaPlayer {
    
    /**
     * isReady and the ..Cache fields are required because setting the audio element's play state 
     * needs to be deferred until the media's metadata has been loaded.
     */
    private boolean isReady = false;
    private double deferredMediaTime;
    private double deferredPlaybackSpeed = 1;
    private boolean deferredIsPlaying;
    private boolean deferredIsMuted;
    
    protected final MediaBase mediaControl;

    public AbstractEmbeddedMediaPlayer(MediaTrack mediaTrack) {
        super(mediaTrack);
        mediaControl = createMediaControl();
        if (mediaControl != null) {
            mediaControl.setControls(false);
            mediaControl.setAutoplay(false);
            mediaControl.setLoop(false);
            mediaControl.setMuted(false);
            mediaControl.setPreload(MediaElement.PRELOAD_AUTO);
            addNativeEventHandlers(mediaControl.getMediaElement());
            mediaControl.setSrc(mediaTrack.url);
            mediaControl.setTitle(mediaTrack.title);
        }
    }
    
    abstract protected MediaBase createMediaControl();

    native void addNativeEventHandlers(MediaElement mediaElement) /*-{
        var that = this;
        mediaElement.addEventListener('canplay', function() {
            that.@com.sap.sailing.gwt.ui.client.media.AbstractEmbeddedMediaPlayer::initPlayState()();
        });
        mediaElement.addEventListener('timeupdate', function(event) {
            that.@com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer::onMediaTimeUpdate()();
        });
    }-*/;
    
    private void initPlayState() {
        if (!isReady && (mediaControl != null)) {
            mediaControl.setCurrentTime(deferredMediaTime);
            mediaControl.setMuted(deferredIsMuted);
            mediaControl.setPlaybackRate(deferredPlaybackSpeed);
            if (deferredIsPlaying) {
                mediaControl.play();
            } else {
                mediaControl.pause();
            }
        }
        isReady = true;
    }

    @Override
    public boolean isMediaPaused() {
        if (isReady) {
            return mediaControl.isPaused();
        } else {
            return !deferredIsPlaying;
        }
    }

    @Override
    public void pauseMedia() {
        deferredIsPlaying = false;
        if (isReady && mediaControl != null) {
            mediaControl.pause();
        }
    }

    @Override
    public void playMedia() {
        deferredIsPlaying = true;
        if (isReady && mediaControl != null) {
            mediaControl.play();
        }
    }

    @Override
    public void setPlaybackSpeed(double playbackSpeed) {
        this.deferredPlaybackSpeed = playbackSpeed; 
        if (isReady && mediaControl != null) {
            mediaControl.setPlaybackRate(playbackSpeed);
        }
    }
    
    @Override
    public void setMuted(boolean isToBeMuted) {
        deferredIsMuted = isToBeMuted;
        if (isReady && mediaControl != null) {
            mediaControl.setMuted(isToBeMuted);
        }
    }

    @Override
    public double getDuration() {
        if (isReady) {
            return mediaControl.getDuration();
        } else {
            return Double.NaN;
        }
    }

    @Override
    public double getCurrentMediaTime() {
        if (isReady) {
            return mediaControl.getCurrentTime();
        } else {
            return Double.NaN;
        }
    }
    
    @Override
    public void setCurrentMediaTime(double mediaTime) {
        this.deferredMediaTime = mediaTime;
        if (isReady && mediaControl != null) {
            mediaControl.setCurrentTime(mediaTime);
        }
    }

    @Override
    public void destroy() {
        mediaControl.pause();
        mediaControl.setSrc(null);
    }

}
