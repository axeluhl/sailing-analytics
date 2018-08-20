package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;

public interface TrackedRaceChangedListener {
    void racesStoppedTracking(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers);

    void racesRemoved(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers);
}
