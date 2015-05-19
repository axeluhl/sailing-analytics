package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SourceElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaSubType;
import com.sap.sailing.domain.common.media.MediaType;
import com.sap.sailing.domain.common.media.MimeType;

public class VideoJSPlayer extends Widget {

    private VideoElement ve;
    private final String elementId;
    private JavaScriptObject player;
    private String type = null;
    private String source = null;
    private boolean autoplay;

    public VideoJSPlayer() {
        this(true, false);
    }

    public VideoJSPlayer(boolean fullHeightWidth, boolean autoplay) {
        this.autoplay = autoplay;
        elementId = "videojs_" + Document.get().createUniqueId();
        ve = Document.get().createVideoElement();
        DivElement div = Document.get().createDivElement();
        setElement(div);
        div.appendChild(ve);
        ve.setId(elementId);
        StringBuilder sb = new StringBuilder("video-js vjs-default-skin vjs-big-play-centered ");
        if (fullHeightWidth) {
            sb.append("video-js-fullscreen");
        }
        ve.setClassName(sb.toString());
        ve.setAttribute("controls", "");


    }



    public void setSource(String source, MimeType type) {

        if (type == null || type.mediaType != MediaType.video) {
            return;
        }



        if (type.mediaSubType == MediaSubType.youtube) {
            this.type = "video/youtube";
        } else if (type.mediaSubType == MediaSubType.vimeo) {
            this.type = "video/vimeo";
        } else if (type.mediaSubType == MediaSubType.mp4) {
            this.type = "video/mp4";
        }
        if (this.type != null) {
            SourceElement se = Document.get().createSourceElement();
            se.setSrc(source);
            se.setType(this.type);
            ve.appendChild(se);
        }
    }




    @Override
    protected void onLoad() {
        _onLoad();

    }

    /**
     * Get the length in time of the video in seconds
     *
     * @return duration in seconds
     */
    public native int getDuration() /*-{
	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player
		.duration();
    }-*/;

    /**
     * Get the current time (in seconds)
     * 
     * @return duration in seconds
     */
    public native int getCurrentTime() /*-{

	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player
		.currentTime();
    }-*/;

    /**
     * Get the current time (in seconds)
     * 
     * @return duration in seconds
     */
    public native void setCurrentTime(int currentTime) /*-{

	return this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player
		.currentTime(currentTime);
    }-*/;

    /**
     * JSNI wrapper that does setup the slider
     *
     * @param uniqueId
     */
    native void _onLoad() /*-{

	var player = $wnd
		.videojs(
			this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::elementId,
			{
			    "width" : "auto",
			    "height" : "auto",

			    "playsInline" : true,
			    "customControlsOnMobile" : true

			}, function() {
			    this.play();

			});

	this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player = player;

    }-*/;

    public native String getThumbnailData() /*-{

	var player = this.@com.sap.sailing.gwt.ui.client.media.VideoJSPlayer::player;

	var oImg = document.createElement("img");
	oImg.setAttribute('src', player.poster());
	oImg.style.display = "none";
	document.body.appendChild(oImg);

	var canvas = document.createElement("canvas");
	canvas.width = oImg.width;
	canvas.height = oImg.height;

	// Copy the image contents to the canvas
	var ctx = canvas.getContext("2d");
	ctx.drawImage(oImg, 0, 0);

	var dataURL = canvas.toDataURL("image/png");

	return dataURL;

    }-*/;

}
