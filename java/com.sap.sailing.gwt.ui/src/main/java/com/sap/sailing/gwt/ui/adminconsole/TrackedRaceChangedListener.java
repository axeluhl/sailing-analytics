package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.server.api.EventNameAndRaceName;

public interface TrackedRaceChangedListener {
    void changeTrackingRace(EventNameAndRaceName eventNameAndRaceName, boolean isTracked);
}
