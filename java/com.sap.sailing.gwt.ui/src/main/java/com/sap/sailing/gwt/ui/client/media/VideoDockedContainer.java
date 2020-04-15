package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.MediaSynchPlayer;

public class VideoDockedContainer extends AbstractMediaContainer implements MediaPlayerContainer {

    public VideoDockedContainer(Panel rootPanel, MediaSynchPlayer videoPlayer, PlayerCloseListener playerCloseListener, PopoutListener popoutListener) {
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
