package com.sap.sailing.gwt.ui.client.media.shared;

import com.sap.sailing.gwt.ui.client.media.MediaSynchAdapter;

public interface VideoSynchPlayer extends VideoPlayerWithWidget, MediaSynchAdapter {
    
    void setEditFlag(EditFlag editFlag);

    int getDefaultWidth();

    int getDefaultHeight();

}
