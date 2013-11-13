package com.sap.sailing.racecommittee.app.services;

import java.util.Collection;

import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.RaceStateEventScheduler;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class RaceStateEventSchedulerOnService implements RaceStateEventScheduler {
    @SuppressWarnings("unused")
    private final static String TAG = RaceStateEventSchedulerOnService.class.getName();

    private final RaceStateService service;
    private final ManagedRace race;

    public RaceStateEventSchedulerOnService(RaceStateService service, ManagedRace race) {
        this.service = service;
        this.race = race;
    }

    @Override
    public void scheduleStateEvents(Collection<RaceStateEvent> stateEvents) {
        for (RaceStateEvent stateEvent : stateEvents) {
            service.setAlarm(race, stateEvent);
        }
    }

    @Override
    public void unscheduleStateEvent(RaceStateEvent stateEvent) {
        service.clearAlarm(race, stateEvent);
    }

    @Override
    public void unscheduleAllEvents() {
        service.clearAllAlarms(race);
    }
}