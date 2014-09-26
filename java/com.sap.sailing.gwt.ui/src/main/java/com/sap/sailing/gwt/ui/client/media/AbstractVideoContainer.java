package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.VideoPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;

public abstract class AbstractVideoContainer {

    protected final PlayerCloseListener popupCloseListener;

    protected final PopoutListener popoutListener;
    protected final VideoSynchPlayer videoPlayer;
    protected final Panel rootPanel;

    public AbstractVideoContainer(Panel rootPanel, VideoSynchPlayer videoPlayer, PopoutListener popoutListener, PlayerCloseListener playerCloseListener) {
        this.rootPanel = rootPanel;
        this.videoPlayer = videoPlayer;
        this.popoutListener = popoutListener;
        this.popupCloseListener = playerCloseListener;
    }

    abstract void hide();
    
    abstract void show();
    
    public void shutDown() {
        videoPlayer.shutDown();
        hide();
    }

    public VideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

}