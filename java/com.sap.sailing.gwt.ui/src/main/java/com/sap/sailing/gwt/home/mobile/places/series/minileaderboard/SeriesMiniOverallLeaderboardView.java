package com.sap.sailing.gwt.home.mobile.places.series.minileaderboard;

import java.util.Collection;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.places.SeriesLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.series.OverallLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.home.shared.places.fakeseries.SeriesContext;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;

public interface SeriesMiniOverallLeaderboardView {

    public interface Presenter extends OverallLeaderboardNavigationProvider, SeriesLeaderboardNavigationProvider {
        DispatchSystem getDispatch();

        SeriesContext getCtx();

        PlaceNavigation<?> getSeriesNavigation();
    }

    Widget asWidget();
    
    void setQuickFinderValues(String seriesName, Collection<EventMetadataDTO> eventsOfSeries);
}
