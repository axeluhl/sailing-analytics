package com.sap.sailing.gwt.ui.client.media.shared;

import com.sap.sailing.gwt.ui.client.media.MediaSynchAdapter;

public interface VideoSynchPlayer extends VideoPlayer, MediaSynchAdapter, WithWidget {
    
    void setEditFlag(EditFlag editFlag);

}
