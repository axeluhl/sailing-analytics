package com.sap.sailing.racecommittee.app.services;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;

public class RaceStateListener implements RaceStateChangedListener {
    @SuppressWarnings("unused")
    private final static String TAG = RaceStateListener.class.getName();

    private final RaceStateService service;
    private final ManagedRace race;

    public RaceStateListener(RaceStateService service, ManagedRace race) {
        this.service = service;
        this.race = race;
    }

    public void onRaceStateChanged(RaceState state) {
        //service.registerAlarms(race, state);
    }

    @Override
    public void onStartTimeChanged(TimePoint startTime) {
        service.handleNewStartTime(race, startTime);
    }

    @Override
    public void onRaceAborted() {
        service.handleRaceAborted(race);
    }

    @Override
    public void onIndividualRecallDisplayed(TimePoint individualRecallRemovalFireTimePoint) {
        service.handleIndividualRecall(race, individualRecallRemovalFireTimePoint);
    }

    @Override
    public void onIndividualRecallRemoval() {
        service.handleIndividualRecallRemoved(race);
    }

    @Override
    public void onAutomaticRaceEnd(TimePoint automaticRaceEnd) {
        service.handleAutomaticRaceEnd(race, automaticRaceEnd);
    }

    @Override
    public void onPathfinderSelected() {
        // do nothing
        
    }
}