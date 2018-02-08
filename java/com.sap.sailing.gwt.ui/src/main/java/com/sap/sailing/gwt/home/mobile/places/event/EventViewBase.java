package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.event.EventReferenceWithStateDTO;
import com.sap.sailing.gwt.home.communication.eventview.EventViewDTO;
import com.sap.sailing.gwt.home.communication.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.NewsItemLinkProvider;
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

    void setQuickFinderValues(Map<String, Set<RegattaMetadataDTO>> regattasByLeaderboardGroupName);

    void setQuickFinderValues(String seriesName, Collection<EventReferenceWithStateDTO> eventsOfSeries);

    void hideQuickfinder();
    
    void setSailorInfos(String description, String buttonLabel, String url);
    
    void setWindfinderNavigations(Iterable<SpotDTO> spotData);

    void setSeriesNavigation(String buttonLabel, PlaceNavigation<?> placeNavigation);

    public interface Presenter extends RegattaLeaderboardNavigationProvider, SeriesLeaderboardNavigationProvider,
            RegattaRacesNavigationProvider, SeriesEventRacesNavigationProvider, RegattaOverviewNavigationProvider,
            SeriesEventOverviewNavigationProvider, NewsItemLinkProvider {
        
        EventContext getCtx();

        SailingDispatchSystem getDispatch();
        
        ErrorAndBusyClientFactory getErrorAndBusyClientFactory();
        
        PlaceNavigation<?> getEventNavigation();
        
        PlaceNavigation<?> getMediaPageNavigation();
        
        String getRaceViewerURL(SimpleRaceMetadataDTO raceMetadata, String mode);

        String getRegattaId();
        
        RegattaMetadataDTO getRegatta();

        EventViewDTO getEventDTO();

        boolean isMultiRegattaEvent();

        boolean isSingleRegattaEvent();
    }
}
