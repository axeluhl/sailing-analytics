package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.media.MediaTrack;

public abstract class AbstractMediaSelectionControl implements MediaPlayerManager.PlayerChangeListener {

    protected final MediaPlayerManager mediaPlayerManager;

    public AbstractMediaSelectionControl(MediaPlayerManager mediaPlayerManager) {
        this.mediaPlayerManager = mediaPlayerManager;
        this.mediaPlayerManager.setPlayerChangeListener(this);
    }

    protected boolean isPotentiallyPlayable(MediaTrack mediaTrack) {
        return MediaTrack.Status.REACHABLE.equals(mediaTrack.status)
                || MediaTrack.Status.UNDEFINED.equals(mediaTrack.status);
    }

    @Override
    public void notifyStateChange() {
        updateUi();
    }

    protected abstract void updateUi();

}