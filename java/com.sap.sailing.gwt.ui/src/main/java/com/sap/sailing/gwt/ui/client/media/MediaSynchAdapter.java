package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.domain.common.media.MediaTrack;

public interface MediaSynchAdapter {

    long getOffset();

    void changeOffsetBy(long delta);

    void setControlsVisible(boolean isVisible);

    void pauseMedia();

    void pauseRace();

    void updateOffset();

    MediaTrack getMediaTrack();

    void forceAlign();

}
