package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.MediaBase;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;
import com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer;

abstract public class AbstractHtmlMediaPlayer extends AbstractMediaPlayer {
    
    /**
     * isReady and the ..Cache fields are required because setting the audio element's play state 
     * needs to be deferred until the media's metadata has been loaded.
     */
    private boolean isReady = false;
    private double deferredMediaTime;
    private double deferredPlaybackSpeed = 1;
    private boolean deferredIsPlaying;
    private boolean deferredIsMuted;
    
    protected final MediaBase mediaElement;

    public AbstractHtmlMediaPlayer(MediaTrackWithSecurityDTO mediaTrack) {
        super(mediaTrack);
        mediaElement = createMediaElement();
        if (mediaElement != null) {
            mediaElement.setControls(false);
            mediaElement.setAutoplay(false);
            mediaElement.setLoop(false);
            mediaElement.setMuted(false);
            mediaElement.setPreload(MediaElement.PRELOAD_AUTO);
            addNativeEventHandlers(mediaElement.getMediaElement());
            mediaElement.setSrc(mediaTrack.url);
            mediaElement.setTitle(mediaTrack.title);
        }
    }
    
    abstract protected MediaBase createMediaElement();

    native void addNativeEventHandlers(MediaElement mediaElement) /*-{
        var that = this;
        mediaElement
                .addEventListener(
                        'canplay',
                        function() {
                            that.@com.sap.sailing.gwt.ui.client.media.AbstractHtmlMediaPlayer::initPlayState()();
                        });
        mediaElement
                .addEventListener(
                        'timeupdate',
                        function(event) {
                            that.@com.sap.sailing.gwt.ui.client.media.shared.AbstractMediaPlayer::onMediaTimeUpdate()();
                        });
    }-*/;
    
    private void initPlayState() {
        if (!isReady && (mediaElement != null)) {
            mediaElement.setCurrentTime(Math.min(deferredMediaTime, mediaElement.getDuration()));
            mediaElement.setMuted(deferredIsMuted);
            mediaElement.setPlaybackRate(deferredPlaybackSpeed);
            if (deferredIsPlaying) {
                mediaElement.play();
            } else {
                mediaElement.pause();
            }
        }
        isReady = true;
    }

    @Override
    public boolean isMediaPaused() {
        if (isReady) {
            return mediaElement.isPaused();
        } else {
            return !deferredIsPlaying;
        }
    }

    @Override
    public void pauseMedia() {
        deferredIsPlaying = false;
        if (isReady && mediaElement != null) {
            mediaElement.pause();
        }
    }

    @Override
    public void playMedia() {
        deferredIsPlaying = true;
        if (isReady && mediaElement != null) {
            mediaElement.play();
        }
    }

    @Override
    public void setPlaybackSpeed(double playbackSpeed) {
        this.deferredPlaybackSpeed = playbackSpeed; 
        if (isReady && mediaElement != null) {
            mediaElement.setPlaybackRate(playbackSpeed);
        }
    }
    
    @Override
    public void setMuted(boolean isToBeMuted) {
        deferredIsMuted = isToBeMuted;
        if (isReady && mediaElement != null) {
            mediaElement.setMuted(isToBeMuted);
        }
    }

    @Override
    public double getDuration() {
        if (isReady) {
            return mediaElement.getDuration();
        } else {
            return Double.NaN;
        }
    }

    @Override
    public double getCurrentMediaTime() {
        if (isReady) {
            return mediaElement.getCurrentTime();
        } else {
            return Double.NaN;
        }
    }
    
    @Override
    public void setCurrentMediaTime(double mediaTime) {
        this.deferredMediaTime = mediaTime;
        if (isReady && mediaElement != null) {
            mediaElement.setCurrentTime(Math.min(mediaTime, mediaElement.getDuration()));
        }
    }

    @Override
    public void shutDown() {
        mediaElement.pause();
        mediaElement.setSrc(null);
    }

}
