package com.sap.sailing.gwt.home.mobile.places.event.media;

import java.util.Collection;

import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.home.communication.media.SailingVideoDTO;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;

public interface MediaView extends EventViewBase {
    
    void setMedia(Collection<SailingVideoDTO> videos, Collection<SailingImageDTO> images);

    public interface Presenter extends EventViewBase.Presenter {
        
    }

}
