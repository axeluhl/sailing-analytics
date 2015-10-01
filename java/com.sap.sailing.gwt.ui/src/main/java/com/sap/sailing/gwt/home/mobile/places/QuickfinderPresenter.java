package com.sap.sailing.gwt.home.mobile.places;

import java.util.Collection;
import java.util.UUID;

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
            final SeriesLeaderboardNavigationProvider navigator, Collection<? extends EventReferenceDTO> eventsOfSeries) {
        return new QuickfinderPresenter(quickfinder, MSG.resultsQuickfinder(), new SeriesEventPlaceNaviationProvider() {
            @Override
            public PlaceNavigation<?> getPlaceNavigation(UUID eventId) {
                return navigator.getMiniLeaderboardNavigation(eventId);
            }
            
            @Override
            public PlaceNavigation<?> getOverallPlaceNavigation() {
                return navigator.getMiniOverallLeaderboardNavigation();
            }
        }, seriesName, eventsOfSeries);
    }
    
    public static QuickfinderPresenter getForSeriesEventRaces(Quickfinder quickfinder, String seriesName,
            final SeriesEventRacesNavigationProvider navigator, Collection<? extends EventReferenceDTO> eventsOfSeries) {
        return new QuickfinderPresenter(quickfinder, MSG.racesQuickfinder(), new SeriesEventPlaceNaviationProvider() {
            @Override
            public PlaceNavigation<?> getPlaceNavigation(UUID eventId) {
                return navigator.getSeriesEventRacesNavigation(eventId);
            }
            
            @Override
            public PlaceNavigation<?> getOverallPlaceNavigation() {
                return null;
            }
        }, seriesName, eventsOfSeries);
    }
    
    public static QuickfinderPresenter getForSeriesEventOverview(Quickfinder quickfinder, String seriesName,
            final SeriesEventOverviewNavigationProvider navigator, Collection<? extends EventReferenceDTO> eventsOfSeries) {
        return new QuickfinderPresenter(quickfinder, MSG.eventQuickfinder(), new SeriesEventPlaceNaviationProvider() {
            @Override
            public PlaceNavigation<?> getPlaceNavigation(UUID eventId) {
                return navigator.getSeriesEventOverviewNavigation(eventId);
            }
            
            @Override
            public PlaceNavigation<?> getOverallPlaceNavigation() {
                return null;
            }
        }, seriesName, eventsOfSeries);
    }
    
    private QuickfinderPresenter(Quickfinder quickfinder, String placeholder, SeriesEventPlaceNaviationProvider provider, 
            String seriesName, Collection<? extends EventReferenceDTO> eventsOfSeries) {
        if (eventsOfSeries == null) {
            quickfinder.removeFromParent();
            return;
        }
        quickfinder.addPlaceholderItem(placeholder);
        PlaceNavigation<?> overallPlaceNavigation = provider.getOverallPlaceNavigation();
        if (overallPlaceNavigation != null) {
            quickfinder.addItemToGroup(seriesName, MSG.overallLeaderboardSelection(), overallPlaceNavigation);
        }
        for (EventReferenceDTO eventOfSeries : eventsOfSeries) {
            String displayName = eventOfSeries.getDisplayName();
            if(eventOfSeries instanceof EventMetadataDTO) {
                displayName = ((EventMetadataDTO) eventOfSeries).getLocationOrDisplayName();
            }
            quickfinder.addItemToGroup(seriesName, displayName, provider.getPlaceNavigation(eventOfSeries.getId()));
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
    
    private interface SeriesEventPlaceNaviationProvider {
        PlaceNavigation<?> getOverallPlaceNavigation();
        PlaceNavigation<?> getPlaceNavigation(UUID eventId);
    }
    
    private interface RegattaPlaceNaviationProvider {
        PlaceNavigation<?> getPlaceNavigation(String regattaId);
    }
}
