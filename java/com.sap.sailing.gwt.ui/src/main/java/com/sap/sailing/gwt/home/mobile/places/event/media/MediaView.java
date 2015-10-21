package com.sap.sailing.gwt.home.mobile.places.event.media;

import java.util.Collection;

import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingVideoDTO;

public interface MediaView extends EventViewBase {
    
    void setMedia(Collection<SailingVideoDTO> videos, Collection<SailingImageDTO> images);

    public interface Presenter extends EventViewBase.Presenter {
        
    }

}
