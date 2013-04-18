package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.http.client.URL;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class VideoWindowPlayer extends PopupWindowPlayer {
    
    public VideoWindowPlayer(MediaTrack mediaTrack, MediaEventHandler videoEventHandler , PopupCloseListener popCloseListener) {
        super(mediaTrack, videoEventHandler, popCloseListener);
    }

    @Override
    protected String getPlayerWindowUrl() {
        String videoUrl = getMediaTrack().url;
        String title = getMediaTrack().title;
        
        String videoPlayerUrl = "/gwt/VideoPopup.html?url=" + URL.encodeQueryString(videoUrl) + "&title=" + URL.encodeQueryString(title);
        
        return videoPlayerUrl;

    }
}
