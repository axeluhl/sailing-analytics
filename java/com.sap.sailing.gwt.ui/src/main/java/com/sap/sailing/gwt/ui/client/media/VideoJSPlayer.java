package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SourceElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.media.MediaSubType;
import com.sap.sse.common.media.MediaType;
import com.sap.sse.common.media.MimeType;

/**
 * video.js (http://videojs.com/) wrapper as GWT widget.
 */
public class VideoJSPlayer extends Widget {
    private static VideoJSPlayerUiBinder uiBinder = GWT.create(VideoJSPlayerUiBinder.class);

    interface VideoJSPlayerUiBinder extends UiBinder<Element, VideoJSPlayer> {
    }

    @UiField VideoElement videoElement;
    
    private final String elementId;
    private JavaScriptObject player;
    private boolean autoplay;

    private Boolean panorama;

    public HandlerRegistration addPlayHandler(PlayEvent.Handler handler) {
        return addHandler(handler, PlayEvent.getType());
    }
    
    public HandlerRegistration addPauseHandler(PauseEvent.Handler handler) {
        return addHandler(handler, PauseEvent.getType());
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

    public void setVideo(MimeType mimeType, String source, boolean panorama) {
        this.panorama = panorama;
        if(isAttached()){
            _onLoad(autoplay, panorama, StringMessages.INSTANCE.threeSixtyVideoHint());
        }
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
    
    @Override
    protected void onLoad() {
        super.onLoad();
        if(panorama != null){
                _onLoad(autoplay, panorama, StringMessages.INSTANCE.threeSixtyVideoHint());
        }
    }
    
    public VideoElement getVideoElement() {
        return videoElement;
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
    
    public native void play() /*-{
        return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.play();
    }-*/;
    
    /**
     * Check whether or not the player is running in full screen mode
     * 
     * @return <code>true</code> if the player is running in full screen mode, <code>false</code> otherwise
     */
    public native boolean isFullscreen() /*-{
        if(this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player == null) {
            return false;
        }
        return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.isFullscreen();
    }-*/;

    /**
     * Check whether the player is currently paused or playing.
     * 
     * @return <code>true</code> if the player is paused, <code>false</code> if it is playing
     */
    public native boolean paused() /*-{
        if(this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player == null) {
            return true;
        }
        return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player.paused();
    }-*/;
    
    private void onPlay() {
        fireEvent(new PlayEvent());
    }
    
    private void onPause() {
        fireEvent(new PauseEvent());
    }
    
    /**
     * JSNI wrapper that does setup the video player
     *
     * @param uniqueId
     */
    native void _onLoad(boolean autoplay, boolean withPanorama, String messageThreeSixty) /*-{
        var that = this;
        var elemid = this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::elementId;
        
        var player = $wnd.videojs(
            elemid,
            {
                "playsInline" : true,
                "customControlsOnMobile" : true
            }).ready(function() {
                this.on('play', function() {
                  that.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::onPlay()();
                });
                this.on('pause', function() {
                  that.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::onPause()();
                });
                
                console.log("play: " + autoplay);
                if (autoplay) {
                    this.play();
                }
            });
       if(withPanorama){     
           player.panorama({
              showNotice:true,
              autoMobileOrientation: true,
              clickAndDrag: true,
              clickToToggle: false,
              NoticeMessage: messageThreeSixty,
            });
        }
        this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player = player;
    }-*/;
    
    native void _onUnload() /*-{
       var player = this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player;
       player.dispose();     
    }-*/;
}
