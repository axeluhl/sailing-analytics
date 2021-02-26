package com.sap.sailing.gwt.home.mobile.places.event.media;

import com.sap.sailing.gwt.home.communication.media.MediaDTO;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;

public interface MediaView extends EventViewBase {
    
    void setMedia(MediaDTO result);

    public interface Presenter extends EventViewBase.Presenter {
        
    }

}
