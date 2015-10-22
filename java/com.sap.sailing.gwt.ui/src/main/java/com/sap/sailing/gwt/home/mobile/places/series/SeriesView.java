package com.sap.sailing.gwt.home.mobile.places.series;

import java.util.Collection;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.home.mobile.places.SeriesLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;

public interface SeriesView {

    Widget asWidget();
    
    void setQuickFinderValues(String seriesName, Collection<EventMetadataDTO> eventsOfSeries);

    public interface Presenter extends OverallLeaderboardNavigationProvider, SeriesLeaderboardNavigationProvider {
        SeriesContext getCtx();

        SailingDispatchSystem getDispatch();
        PlaceNavigation<?> getEventNavigation(String eventId);
    }
}

