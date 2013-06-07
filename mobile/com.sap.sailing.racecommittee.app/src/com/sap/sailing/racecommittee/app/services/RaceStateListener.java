package com.sap.sailing.racecommittee.app.services;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateEventListener;

public class RaceStateListener implements RaceStateEventListener {
    @SuppressWarnings("unused")
    private final static String TAG = RaceStateListener.class.getName();

    private final RaceStateService service;
    private final ManagedRace race;

    public RaceStateListener(RaceStateService service, ManagedRace race) {
        this.service = service;
        this.race = race;
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
    public void onStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId) {
        service.handleStartProcedureSpecificEvent(race, eventTime, eventId);
    }
}