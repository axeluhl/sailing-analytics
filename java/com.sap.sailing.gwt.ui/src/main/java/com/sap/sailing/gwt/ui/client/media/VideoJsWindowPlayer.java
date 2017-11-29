package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.gwt.client.player.Timer;

public class VideoJsWindowPlayer implements VideoContainer {
    private VideoJSSyncPlayer videoJsDelegate;

    public VideoJsWindowPlayer(MediaTrack videoTrack, TimePoint raceStartTime, Timer raceTimer,
            PlayerCloseListener playerCloseListener) {
        videoJsDelegate = new VideoJSSyncPlayer(videoTrack, raceStartTime, raceTimer);
    }

    @Override
    public void shutDown() {
        videoJsDelegate.shutDown();
    }

    @Override
    public VideoPlayer getVideoPlayer() {
        return videoJsDelegate;
    }

}
