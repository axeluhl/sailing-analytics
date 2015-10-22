package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.event.EventReferenceDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.mobile.places.RegattaLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.RegattaOverviewNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.RegattaRacesNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.SeriesEventOverviewNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.SeriesEventRacesNavigationProvider;
import com.sap.sailing.gwt.home.mobile.places.SeriesLeaderboardNavigationProvider;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;

public interface EventViewBase extends IsWidget {

    void setQuickFinderValues(Collection<RegattaMetadataDTO> regattaMetadatas);

    void setQuickFinderValues(String seriesName, Collection<EventReferenceDTO> eventsOfSeries);

    void hideQuickfinder();
    
    void setSailorInfos(String description, String buttonLabel, String url);
    
    void setSeriesNavigation(String buttonLabel, PlaceNavigation<?> placeNavigation);

    public interface Presenter extends RegattaLeaderboardNavigationProvider, SeriesLeaderboardNavigationProvider,
            RegattaRacesNavigationProvider, SeriesEventRacesNavigationProvider, RegattaOverviewNavigationProvider,
            SeriesEventOverviewNavigationProvider {
        
        EventContext getCtx();

        SailingDispatchSystem getDispatch();
        
        ErrorAndBusyClientFactory getErrorAndBusyClientFactory();
        
        PlaceNavigation<?> getEventNavigation();

        String getRegattaId();
        
        RegattaMetadataDTO getRegatta();

        EventViewDTO getEventDTO();
    }
}
