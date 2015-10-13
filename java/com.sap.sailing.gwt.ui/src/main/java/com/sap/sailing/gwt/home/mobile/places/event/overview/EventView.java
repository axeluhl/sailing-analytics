package com.sap.sailing.gwt.home.mobile.places.event.overview;

import java.util.Collection;

import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public interface EventView extends EventViewBase {

    void setMediaForImpressions(int nrOfImages, int nrOfVideos, Collection<SailingImageDTO> images);

    public interface Presenter extends EventViewBase.Presenter, NewsItemLinkProvider {
        PlaceNavigation<?> getMediaPageNavigation();
    }
}

