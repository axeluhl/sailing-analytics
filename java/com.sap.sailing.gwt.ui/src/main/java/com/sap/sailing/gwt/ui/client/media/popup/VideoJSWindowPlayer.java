package com.sap.sailing.gwt.ui.client.media.popup;

import com.google.gwt.http.client.URL;
import com.sap.sailing.domain.common.media.MediaTrack;

public class VideoJSWindowPlayer extends PopoutWindowPlayer {

    public VideoJSWindowPlayer(MediaTrack mediaTrack, PlayerCloseListener popupCloseListener) {
        super(mediaTrack, popupCloseListener);
    }

    @Override
    protected String getPlayerWindowUrl() {
        String videoUrl = getMediaTrack().url;
        String title = getMediaTrack().title;
        String mimetype = getMediaTrack().mimeType == null ? "" : getMediaTrack().mimeType.name();
        String videoPlayerUrl = "/gwt/VideoPopup.html?url=" + URL.encodeQueryString(videoUrl) + "&title="
                + URL.encodeQueryString(title) + "&mimetype=" + URL.encodeQueryString(String.valueOf(mimetype));

        return videoPlayerUrl;

    }
}
