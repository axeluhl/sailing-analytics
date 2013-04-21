package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.common.TimePoint;

public interface RaceStateChangedListener {
    void onRaceStateChanged(RaceState state);
    
    void onStartTimeChanged(TimePoint startTime);
    
    void onRaceAborted();
    
    void onIndividualRecallDisplayed(TimePoint individualRecallRemovalFireTimePoint);

    void onIndividualRecallRemoval();

    void onAutomaticRaceEnd(TimePoint automaticRaceEnd);
    
    void onPathfinderSelected();
}