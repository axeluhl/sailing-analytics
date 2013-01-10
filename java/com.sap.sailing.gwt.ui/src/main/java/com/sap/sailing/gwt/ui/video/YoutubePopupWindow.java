package com.sap.sailing.gwt.ui.video;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class YoutubePopupWindow extends AbstractPopupWindow {

    private static final String VIDEO_CONTAINER_ID = "videoContainer";

    private JavaScriptObject youtubePlayer;

    @Override
    protected void initializePlayer() {
        String title = Window.Location.getParameter("title");
        Window.setTitle(title);

        RootLayoutPanel.get().getElement().setId(VIDEO_CONTAINER_ID);

        String videoUrl = Window.Location.getParameter("id");
        if (videoUrl != null) {
            loadYoutube(videoUrl, VIDEO_CONTAINER_ID);
        }
    }

    // Inspired by https://developers.google.com/youtube/iframe_api_reference#Examples
    // See also: https://developers.google.com/youtube/js_api_reference
    // Code Playground: https://code.google.com/apis/ajax/playground/?exp=youtube#chromeless_player
    private native void loadYoutube(String videoUrl, String videoContainerId) /*-{
        
		var that = this;

		// This is a protocol-relative URL as described here: http://paulirish.com/2010/the-protocol-relative-url/
		var tag = $doc.createElement('script');
		tag.src = "//www.youtube.com/iframe_api";
		var firstScriptTag = $doc.getElementsByTagName('script')[0];
		firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

		// This function creates an <iframe> containing a YouTube player after the API code downloads.
		var player;
		$wnd.onYouTubeIframeAPIReady = function() {
			var player = new $wnd.YT.Player(videoContainerId, {
				videoId : videoUrl,
                                height: '480', //see https://developers.google.com/youtube/iframe_api_reference?hl=en#Playback_quality
                                width: '853',
                                events: { //https://developers.google.com/youtube/iframe_api_reference?hl=en#Events
                                    'onReady': function(event) {
                                                    var deferredPlayState = $wnd.deferredPlayState
                                                    if (deferredPlayState && !$wnd.videoPlayer) {
                                                        that.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::initPlayState(DZDZ)(deferredPlayState.deferredMediaTime, deferredPlayState.isDeferredMuted, deferredPlayState.deferredPlaybackSpeed, deferredPlayState.isDeferredPlaying);
                                                    }
                                               }
                                },
				playerVars : {
					'autoplay' : 0,
					'controls' : 0
				}
			});
//			player.addEventListener("onStateChange", "handleStateChange");
			that.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::setYoutubePlayer(Lcom/google/gwt/core/client/JavaScriptObject;)(player);
		}

    }-*/;

    public void setYoutubePlayer(JavaScriptObject youtubePlayer) {
        this.youtubePlayer = youtubePlayer;
    }
    
    @Override
    public void initPlayState(double deferredMediaTime, boolean isDeferredMuted, double deferredPlaybackSpeed, boolean isDeferredPlaying) {
        adjustWindowSize();
        super.initPlayState(deferredMediaTime, isDeferredMuted, deferredPlaybackSpeed, isDeferredPlaying);
    }

    public native void play() /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		player.playVideo();
    }-*/;

    public native void pause() /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		player.pauseVideo();
    }-*/;

    public native void setTime(double time) /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		var allowSeekAhead = true; 
		player.seekTo(time, allowSeekAhead);
    }-*/;

    public native void setMuted(boolean muted) /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		if (muted) {
		    player.mute();
		} else {
		    player.unMute()
		}
    }-*/;

    public native boolean isPaused() /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		return player.getPlayerState() == $wnd.YT.PlayerState.PAUSED;
    }-*/;

    public native double getDuration() /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		return player.getDuration();
    }-*/;

    public native double getTime() /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		return player.getCurrentTime();
    }-*/;

    public native void setPlaybackSpeed(double newPlaySpeedFactor) /*-{
		var player = this.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::youtubePlayer;
		player.setPlaybackRate(newPlaySpeedFactor);
    }-*/;

    @Override
    protected boolean hasVideoSizes() {
        return youtubePlayer != null;
    }
    
    @Override
    protected int getVideoWidth() {
        return RootPanel.get(VIDEO_CONTAINER_ID).getOffsetWidth();
    }

    @Override
    protected int getVideoHeight() {
        return RootPanel.get(VIDEO_CONTAINER_ID).getOffsetHeight();
    }

    @Override
    protected void setVideoSize(int width, int height) {
        RootPanel.get(VIDEO_CONTAINER_ID).setPixelSize(width, height);
    }

}
