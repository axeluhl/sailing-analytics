package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.mobile.places.RegattaLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.SeriesLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public interface EventViewBase extends IsWidget {

    void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas);

    void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries);

    void hideQuickfinder();

    public interface Presenter extends RegattaLeaderboardNavigationProvider, SeriesLeaderboardNavigationProvider {
        
        PlaceNavigation<?> getEventNavigation();

        DispatchSystem getDispatch();

        EventContext getCtx();
    }
}
