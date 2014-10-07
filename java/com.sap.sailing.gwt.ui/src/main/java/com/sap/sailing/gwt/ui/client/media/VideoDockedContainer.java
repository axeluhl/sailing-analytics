package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;

public class VideoDockedContainer extends AbstractVideoContainer implements VideoContainer {

    public VideoDockedContainer(Panel rootPanel, VideoSynchPlayer videoPlayer, PlayerCloseListener playerCloseListener, PopoutListener popoutListener) {
        super(rootPanel, videoPlayer, popoutListener, playerCloseListener);
    }

    @Override
    void show() {
        rootPanel.setVisible(true);
    }

    @Override
    void hide() {
        rootPanel.setVisible(true);
    }

}
