package com.sap.sailing.gwt.ui.client.media.shared;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.ui.client.media.MediaSynchAdapter;

public interface MediaSynchPlayer extends IsWidget, MediaPlayer, MediaSynchAdapter {
    
    void setEditFlag(EditFlag editFlag);

    int getDefaultWidth();

    int getDefaultHeight();

}
