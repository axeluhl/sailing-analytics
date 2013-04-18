package com.sap.sailing.gwt.ui.client.media;

import java.util.UUID;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class YoutubeVideoControl  {

    private final String VIDEO_CONTAINER_ID = "videoContainer-" + UUID.randomUUID();

    private JavaScriptObject youtubePlayer;

    private final Panel containerPanel;
    
    private boolean deferredIsPlaying;

    private double currentTime;

    private boolean deferredIsMuted;

    private boolean deferredIsControlsVisible;

    YoutubeVideoControl(String videoUrl) {
        
        containerPanel = new SimplePanel();

        containerPanel.getElement().setId(VIDEO_CONTAINER_ID);

        loadYoutubePlayer(videoUrl, VIDEO_CONTAINER_ID);
    }

    // Inspired by https://developers.google.com/youtube/iframe_api_reference#Examples
    // See also: https://developers.google.com/youtube/js_api_reference
    // Code Playground: https://code.google.com/apis/ajax/playground/?exp=youtube#chromeless_player
    private native void loadYoutubePlayer(String videoUrl, String videoContainerId) /*-{
        
                var that = this;

                // This function creates an <iframe> containing a YouTube player after the API code downloads.
                var player;
                $wnd.onYouTubeIframeAPIReady = function() {
                        var player = new $wnd.YT.Player(videoContainerId, {
                                videoId : videoUrl,
                                height: '480', //see https://developers.google.com/youtube/iframe_api_reference?hl=en#Playback_quality
                                width: '853',
                                events: { //https://developers.google.com/youtube/iframe_api_reference?hl=en#Events
                                    'onReady': function(event) {
                                                    that.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::initPlayState(Lcom/google/gwt/core/client/JavaScriptObject;)(player);
                                               }
                                },
                                playerVars : {
                                        'autoplay' : 0,
                                        'controls' : 1
                                }
                        });
                }

                // Use script tag trick to cope with browser's cross domain restrictions
                var tag = $doc.createElement('script');
                tag.src = "//www.youtube.com/iframe_api"; // This is a protocol-relative URL as described here: http://paulirish.com/2010/the-protocol-relative-url/
                var firstScriptTag = $doc.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

    }-*/;

    public void initPlayState(JavaScriptObject youtubePlayer) {
        this.youtubePlayer = youtubePlayer;
    }
    
    public void play() {
        if (this.youtubePlayer != null) {
            nativePlay();
        } else {
            this.deferredIsPlaying = true;
        }
    }
    
    private native void nativePlay() /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                player.playVideo();
    }-*/;
    
    public void pause() {
        if (this.youtubePlayer != null) {
            nativePause();
        } else {
            this.deferredIsPlaying = false;
        }
    }

    private native void nativePause() /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                player.pauseVideo();
    }-*/;

    public void setCurrentTime(double time) {
        if (this.youtubePlayer != null) {
            setCurrentTime(time);
        } else {
            this.currentTime = time;
        }
    }
    
    private native void nativeSetCurrentTime(double time) /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                var allowSeekAhead = true; 
                player.seekTo(time, allowSeekAhead);
    }-*/;

    public void setMuted(boolean muted) {
        if (this.youtubePlayer != null) {
            nativeSetMuted(muted);
        } else {
            this.deferredIsMuted = muted;
        }
    }
    
    private native void nativeSetMuted(boolean muted) /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                if (muted) {
                    player.mute();
                } else {
                    player.unMute()
                }
    }-*/;

    public native boolean isPaused() /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                return player.getPlayerState() == $wnd.YT.PlayerState.PAUSED;
    }-*/;

    public native double getDuration() /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                return player.getDuration();
    }-*/;

    public native double getCurrentTime() /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                return player.getCurrentTime();
    }-*/;

    public native void setPlaybackSpeed(double newPlaySpeedFactor) /*-{
                var player = this.@com.sap.sailing.gwt.ui.client.media.YoutubeVideoControl::youtubePlayer;
                player.setPlaybackRate(newPlaySpeedFactor);
    }-*/;

    public Widget widget() {
        return containerPanel;
    }

    public void setControlsVisible(boolean isVisible) {
        if (this.youtubePlayer != null) {
            nativeSetControlsVisible(isVisible);
        } else {
            this.deferredIsControlsVisible = isVisible;
        }
    }
    
    native void nativeSetControlsVisible(boolean isVisible) /*-{
        //TODO: find respsective property in the player API
    }-*/;

}
