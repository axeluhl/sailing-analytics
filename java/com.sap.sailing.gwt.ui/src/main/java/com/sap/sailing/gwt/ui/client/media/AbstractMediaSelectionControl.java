package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;

public abstract class AbstractMediaSelectionControl implements MediaPlayerManager.PlayerChangeListener {

    protected final AgentTypes userAgent;
    protected final MediaPlayerManager mediaPlayerManager;

    public AbstractMediaSelectionControl(MediaPlayerManager mediaPlayerManager, AgentTypes userAgent) {
        this.mediaPlayerManager = mediaPlayerManager;
        this.mediaPlayerManager.setPlayerChangeListener(this);
        this.userAgent = userAgent;
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