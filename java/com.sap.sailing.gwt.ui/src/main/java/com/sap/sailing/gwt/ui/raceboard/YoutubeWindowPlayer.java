package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.http.client.URL;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class YoutubeWindowPlayer extends PopupWindowPlayer {
    
    public YoutubeWindowPlayer(MediaTrack mediaTrack, PopupCloseListener popupCloseListener) {
        super(mediaTrack, popupCloseListener);
    }

    @Override
    protected String getPlayerWindowUrl() {
        String youtubeId = getMediaTrack().url;
        String title = getMediaTrack().title;
        
        String youtubePlayerUrl = "/gwt/YoutubePopup.html?id=" + URL.encodeQueryString(youtubeId) + "&title=" + URL.encodeQueryString(title);
        
        return youtubePlayerUrl;
    }
    
}
