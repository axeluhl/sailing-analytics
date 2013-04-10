package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public interface TrackedRaceChangedListener {
    void changeTrackingRace(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, boolean isTracked);
}
