package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.video;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.gwt.client.media.VideoDTO;

public interface VideoView {

    void startingWith(VideoPresenter p, AcceptsOneWidget panel);

    void playVideo(VideoDTO video);

    public interface VideoPresenter {
        void publishDuration(int duration);
    }

}
