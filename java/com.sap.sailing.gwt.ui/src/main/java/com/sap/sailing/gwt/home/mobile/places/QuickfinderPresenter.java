package com.sap.sailing.gwt.home.mobile.places;

import java.util.Collection;

import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class QuickfinderPresenter {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    
    public static QuickfinderPresenter getForRegattaLeaderboards(Quickfinder quickfinder, 
            final RegattaLeaderboardNavigationProvider navigator, Collection<RegattaMetadataDTO> regattaMetadatas) {
        return new QuickfinderPresenter(quickfinder, MSG.resultsQuickfinder(), new RegattaPlaceNaviationProvider() {
            @Override
            public PlaceNavigation<?> getPlaceNavigation(String regattaId) {
                return navigator.getRegattaMiniLeaderboardNavigation(regattaId);
            }
        }, regattaMetadatas);
    }
    
    public static QuickfinderPresenter getForRegattaRaces(Quickfinder quickfinder, 
            final RegattaRacesNavigationProvider navigator, Collection<RegattaMetadataDTO> regattaMetadatas) {
        return new QuickfinderPresenter(quickfinder, MSG.racesQuickfinder(), new RegattaPlaceNaviationProvider() {
            @Override
            public PlaceNavigation<?> getPlaceNavigation(String regattaId) {
                return navigator.getRegattaRacesNavigation(regattaId);
            }
        }, regattaMetadatas);
    }
    
    public static QuickfinderPresenter getForRegattaOverview(Quickfinder quickfinder, 
            final RegattaOverviewNavigationProvider navigator, Collection<RegattaMetadataDTO> regattaMetadatas) {
        return new QuickfinderPresenter(quickfinder, MSG.regattaQuickfinder(), new RegattaPlaceNaviationProvider() {
            @Override
            public PlaceNavigation<?> getPlaceNavigation(String regattaId) {
                return navigator.getRegattaOverviewNavigation(regattaId);
            }
        }, regattaMetadatas);
    }
    
    public static QuickfinderPresenter getForSeriesLeaderboards(Quickfinder quickfinder, String seriesName,
            SeriesLeaderboardNavigationProvider navigator, Collection<? extends EventReferenceDTO> eventsOfSeries) {
        return new QuickfinderPresenter(quickfinder, navigator, seriesName, eventsOfSeries);
    }
    
    private QuickfinderPresenter(Quickfinder quickfinder, SeriesLeaderboardNavigationProvider navigator, String seriesName, Collection<? extends EventReferenceDTO> eventsOfSeries) {
        if (eventsOfSeries == null) {
            quickfinder.removeFromParent();
            return;
        }
        quickfinder.addPlaceholderItem(MSG.resultsQuickfinder());
        quickfinder.addItemToGroup(seriesName, MSG.overallLeaderboardSelection(), navigator.getMiniOverallLeaderboardNavigation());
        for (EventReferenceDTO eventOfSeries : eventsOfSeries) {
            String displayName = eventOfSeries.getDisplayName();
            if(eventOfSeries instanceof EventMetadataDTO) {
                displayName = ((EventMetadataDTO) eventOfSeries).getLocationOrDisplayName();
            }
            quickfinder.addItemToGroup(seriesName, displayName, navigator.getMiniLeaderboardNavigation(eventOfSeries.getId()));
        }
    }
    
    private QuickfinderPresenter(Quickfinder quickfinder, String placeholder, RegattaPlaceNaviationProvider provider, 
            Collection<RegattaMetadataDTO> regattaMetadatas) {
        if (regattaMetadatas == null) {
            quickfinder.removeFromParent();
            return;
        }
        quickfinder.addPlaceholderItem(placeholder);
        for (RegattaMetadataDTO regattaMetadata : regattaMetadatas) {
            String boatCategory = regattaMetadata.getBoatCategory();
            if(boatCategory == null || boatCategory.isEmpty()) {
                boatCategory = MSG.regattas();
            }
            quickfinder.addItemToGroup(boatCategory, regattaMetadata.getDisplayName(), provider.getPlaceNavigation(regattaMetadata.getId()));
        }
    }
    
    private interface RegattaPlaceNaviationProvider {
        PlaceNavigation<?> getPlaceNavigation(String regattaId);
    }
}
