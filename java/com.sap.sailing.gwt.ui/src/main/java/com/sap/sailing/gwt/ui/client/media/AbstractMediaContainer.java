package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.ui.client.media.popup.PopoutWindowPlayer.PlayerCloseListener;
import com.sap.sailing.gwt.ui.client.media.shared.MediaSynchPlayer;

public abstract class AbstractMediaContainer implements MediaPlayerContainer {

    protected final PlayerCloseListener popupCloseListener;

    protected final PopoutListener popoutListener;
    protected final MediaSynchPlayer mediaPlayer;
    protected final Panel rootPanel;

    public AbstractMediaContainer(Panel rootPanel, MediaSynchPlayer mediaPlayer, PopoutListener popoutListener,
            PlayerCloseListener playerCloseListener) {
        this.rootPanel = rootPanel;
        this.mediaPlayer = mediaPlayer;
        this.popoutListener = popoutListener;
        this.popupCloseListener = playerCloseListener;
    }

    abstract void hide();
    
    abstract void show();
    
    @Override
    public void shutDown() {
        mediaPlayer.shutDown();
        hide();
    }

    @Override
    public MediaSynchPlayer getMediaPlayer() {
        return mediaPlayer;
    }

}