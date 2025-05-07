package com.sap.sailing.gwt.ui.client.media.popup;

import java.util.UUID;

import com.google.gwt.http.client.URL;
import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public class VideoJSWindowPlayer extends PopoutWindowPlayer {

    private final UUID eventId;
    private final String leaderboardGroupName;

    public VideoJSWindowPlayer(MediaTrackWithSecurityDTO mediaTrack, PlayerCloseListener popupCloseListener, String leaderboardGroupName, UUID eventId) {
        super(mediaTrack, popupCloseListener);
        this.leaderboardGroupName = leaderboardGroupName;
        this.eventId = eventId;
    }

    @Override
    protected String getPlayerWindowUrl() {
        String videoUrl = getMediaTrack().url;
        String title = getMediaTrack().title;
        String mimetype = getMediaTrack().mimeType == null ? "" : getMediaTrack().mimeType.name();
        String videoPlayerUrl = "/gwt/VideoPopup.html?url=" + URL.encodeQueryString(videoUrl) + "&title="
                + URL.encodeQueryString(title) + "&mimetype=" + URL.encodeQueryString(String.valueOf(mimetype))
                + "&eventName=" + URL.encodeQueryString(""+eventId+"/"+leaderboardGroupName);
        return videoPlayerUrl;
    }
}
