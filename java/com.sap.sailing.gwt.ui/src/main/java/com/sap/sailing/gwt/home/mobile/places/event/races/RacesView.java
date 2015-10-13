package com.sap.sailing.gwt.home.mobile.places.event.races;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.home.mobile.places.event.EventViewBase;

public interface RacesView extends EventViewBase {

    public interface Presenter extends EventViewBase.Presenter {
        String getRaceViewerURL(String leaderboardName, RegattaAndRaceIdentifier raceIdentifier);
    }

}
