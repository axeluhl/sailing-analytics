package com.sap.sailing.gwt.home.mobile.places;

import java.util.Collection;

import com.sap.sailing.gwt.home.mobile.partials.quickfinder.Quickfinder;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventReferenceDTO;

public class QuickfinderPresenter {
    private static final StringMessages MSG = StringMessages.INSTANCE;
    

    public QuickfinderPresenter(Quickfinder quickfinder, RegattaLeaderboardNavigationProvider navigator, Collection<RegattaMetadataDTO> regattaMetadatas) {
        if (regattaMetadatas == null) {
            quickfinder.removeFromParent();
            return;
        }
        quickfinder.addPlaceholderItem(MSG.resultsQuickfinder());
        for (RegattaMetadataDTO regattaMetadata : regattaMetadatas) {
            quickfinder.addItemToGroup(regattaMetadata.getBoatCategory(), regattaMetadata.getDisplayName(), navigator.getRegattaMiniLeaderboardNavigation(regattaMetadata.getId()));
        }
    }
    
    public QuickfinderPresenter(Quickfinder quickfinder, SeriesLeaderboardNavigationProvider navigator, String seriesName, Collection<? extends EventReferenceDTO> eventsOfSeries) {
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

}
