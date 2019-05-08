package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.media.MediaTrackWithSecurityDTO;

public interface MediaSynchAdapter {
    
    public interface EditFlag {
        boolean isEditing();
    }

    long getOffset();

    void changeOffsetBy(long delta);

    void setControlsVisible(boolean isVisible);

    void pauseMedia();

    void pauseRace();

    void updateOffset();

    MediaTrackWithSecurityDTO getMediaTrack();

    void forceAlign();

}
