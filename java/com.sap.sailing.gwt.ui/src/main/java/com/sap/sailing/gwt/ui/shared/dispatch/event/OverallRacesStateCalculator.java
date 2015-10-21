package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceViewState;

public class OverallRacesStateCalculator implements RaceCallback {
    private boolean hasLiveRace = false;
    private boolean hasFinishedRace = false;
    private boolean hasAbandonedOrPostponedRace = false;
    private boolean hasUnfinishedRace = false;

    @Override
    public void doForRace(RaceContext context) {
        RaceViewState raceViewState = context.getLiveRaceViewState();
        hasLiveRace |= raceViewState == RaceViewState.RUNNING;
        hasFinishedRace |= raceViewState == RaceViewState.FINISHED;
        hasAbandonedOrPostponedRace |= raceViewState == RaceViewState.POSTPONED || raceViewState == RaceViewState.ABANDONED;
        hasUnfinishedRace |= raceViewState != RaceViewState.FINISHED;
    }

    public boolean hasLiveRace() {
        return hasLiveRace;
    }

    public boolean hasFinishedRace() {
        return hasFinishedRace;
    }
    
    public boolean hasUnfinishedRace() {
        return hasUnfinishedRace;
    }

    public boolean hasAbandonedOrPostponedRace() {
        return hasAbandonedOrPostponedRace;
    }
}
