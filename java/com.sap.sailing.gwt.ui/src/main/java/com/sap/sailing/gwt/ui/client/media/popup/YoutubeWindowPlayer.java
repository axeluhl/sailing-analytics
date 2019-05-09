package com.sap.sailing.gwt.ui.client.media.popup;

import com.google.gwt.http.client.URL;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public class YoutubeWindowPlayer extends PopoutWindowPlayer {
    
    public YoutubeWindowPlayer(MediaTrackWithSecurityDTO mediaTrack, PlayerCloseListener popupCloseListener) {
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
