package com.sap.sailing.racecommittee.app.services;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEventScheduler;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class RaceStateEventSchedulerOnService implements RaceStateEventScheduler {

    private final RaceStateService service;
    private final ManagedRace race;

    public RaceStateEventSchedulerOnService(RaceStateService service, ManagedRace race) {
        this.service = service;
        this.race = race;
    }

    @Override
    public void scheduleStateEvents(Iterable<RaceStateEvent> stateEvents) {
        for (RaceStateEvent stateEvent : stateEvents) {
            service.setAlarm(race, stateEvent);
        }
    }

    @Override
    public void unscheduleStateEvent(RaceStateEvents stateEventName) {
        service.clearAlarmByName(race, stateEventName);
    }

    @Override
    public void unscheduleAllEvents() {
        service.clearAllAlarms(race);
    }
}