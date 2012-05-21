package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.RegattaNameAndRaceName;

public interface TrackedRaceChangedListener {
    void changeTrackingRace(RegattaNameAndRaceName regattaNameAndRaceName, boolean isTracked);
}
