package com.sap.sailing.gwt.home.mobile.places.series;

import java.util.Collection;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.places.SeriesLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public interface SeriesView {

    Widget asWidget();
    
    void setQuickFinderValues(String seriesName, Collection<EventMetadataDTO> eventsOfSeries);

    public interface Presenter extends OverallLeaderboardNavigationProvider, SeriesLeaderboardNavigationProvider {
        SeriesContext getCtx();
        DispatchSystem getDispatch();
        PlaceNavigation<?> getEventNavigation(String eventId);
    }
}

