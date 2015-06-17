package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;

public interface EventView {

    Widget asWidget();
    
    void setSailorInfos(String description, String buttonLabel, String url);
    
    void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas);

    void setMediaForImpressions(int nrOfImages, int nrOfVideos, List<SailingImageDTO> images);

    public interface Presenter extends NewsItemLinkProvider {
        EventContext getCtx();

        DispatchSystem getDispatch();

        PlaceNavigation<?> getRegattaLeaderboardNavigation(String leaderboardName);

        String getRaceViewerURL(String regattaName, String trackedRaceName);

        PlaceNavigation<?> getMediaPageNavigation();
    }
}

