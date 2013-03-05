package com.sap.sailing.gwt.ui.raceboard;

public interface MediaSynchListener {

    long getOffset();

    void setOffset(long offset);

    void setControlsVisible(boolean isVisible);

    void save();

    void discard();

}
