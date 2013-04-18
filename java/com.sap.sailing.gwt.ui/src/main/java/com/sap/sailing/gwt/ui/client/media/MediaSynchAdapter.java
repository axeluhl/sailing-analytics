package com.sap.sailing.gwt.ui.client.media;

public interface MediaSynchAdapter {

    long getOffset();

    void changeOffsetBy(long delta);

    void setControlsVisible(boolean isVisible);

    void save();

    void discard();

    void pauseMedia();

    void pauseRace();

    void updateOffset();

}
