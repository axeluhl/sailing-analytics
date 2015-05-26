package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SourceElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaSubType;
import com.sap.sailing.domain.common.media.MediaType;
import com.sap.sailing.domain.common.media.MimeType;

public class VideoJSPlayer extends Widget {
    
    private static VideoJSPlayerUiBinder uiBinder = GWT.create(VideoJSPlayerUiBinder.class);

    interface VideoJSPlayerUiBinder extends UiBinder<Element, VideoJSPlayer> {
    }

    @UiField VideoElement videoElement;
    
    private final String elementId;
    private JavaScriptObject player;
    private boolean autoplay;

    public VideoJSPlayer() {
        this(true, false);
    }

    public VideoJSPlayer(boolean fullHeightWidth, boolean autoplay) {
        this.autoplay = autoplay;
        setElement(uiBinder.createAndBindUi(this));
        videoElement.setId(elementId = "videojs_" + Document.get().createUniqueId());
        if (fullHeightWidth) {
            videoElement.addClassName("video-js-fullscreen");
        }
        videoElement.setAttribute("controls", "");
    }

    public void setSource(String source, MimeType mimeType) {
        if (mimeType == null || mimeType.mediaType != MediaType.video) {
            return;
        }
        String type = null;
        if (mimeType.mediaSubType == MediaSubType.youtube) {
            type = "video/youtube";
        } else if (mimeType.mediaSubType == MediaSubType.vimeo) {
            type = "video/vimeo";
        } else if (mimeType.mediaSubType == MediaSubType.mp4) {
            type = "video/mp4";
        }
        if (type != null) {
            SourceElement se = Document.get().createSourceElement();
            se.setSrc(source);
            se.setType(type);
            videoElement.appendChild(se);
        }
    }
    
    public VideoElement getVideoElement() {
        return videoElement;
    }
    
    @Override
    protected void onLoad() {
        _onLoad(autoplay);
    }

    /**
     * Get the length in time of the video in seconds
     *
     * @return duration in seconds
     */
    public native int getDuration() /*-{
	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.duration();
    }-*/;

    /**
     * Get the current time (in seconds)
     * 
     * @return duration in seconds
     */
    public native int getCurrentTime() /*-{
        return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.currentTime();
    }-*/;

    /**
     * Get the current time (in seconds)
     * 
     * @return duration in seconds
     */
    public native void setCurrentTime(int currentTime) /*-{
	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.currentTime(currentTime);
    }-*/;
    
    /**
     * Check whether or not the player is running in full screen mode
     * 
     * @return <code>true</code> if the player is running in full screen mode, <code>false</code> otherwise
     */
    public native boolean isFullscreen() /*-{
	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.isFullscreen();
    }-*/;

    /**
     * Check whether the player is currently paused or playing.
     * 
     * @return <code>true</code> if the player is paused, <code>false</code> if it is playing
     */
    public native boolean paused() /*-{
	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.paused();
    }-*/;
    
    /**
     * JSNI wrapper that does setup the video player
     *
     * @param uniqueId
     */
    native void _onLoad(boolean autoplay) /*-{
	var player = $wnd.videojs(
	    this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::elementId,
	    {
		"width" : "auto",
		"height" : "auto",
		"playsInline" : true,
		"customControlsOnMobile" : true
	    }, 
	    function() {
		console.log("play: " + autoplay);
		if (autoplay) {
		    this.play();
		}
	    });
	this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player = player;
    }-*/;
    
    native void _onUnload() /*-{
       var player = this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player;
       player.dispose();     
    }-*/;

}
