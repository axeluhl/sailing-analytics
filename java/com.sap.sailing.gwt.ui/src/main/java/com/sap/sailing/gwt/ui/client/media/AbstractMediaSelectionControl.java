package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractMediaSelectionControl implements MediaPlayerManager.PlayerChangeListener {

    protected final MediaPlayerManager mediaPlayerManager;
    protected final StringMessages stringMessages;

    public AbstractMediaSelectionControl(MediaPlayerManager mediaPlayerManager, StringMessages stringMessages) {
        this.mediaPlayerManager = mediaPlayerManager;
        this.mediaPlayerManager.addPlayerChangeListener(this);
        this.stringMessages = stringMessages;
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