package com.sap.sailing.domain.abstractlog.race.state;

import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;

public interface RaceStateEventScheduler {
 
    void scheduleStateEvents(Iterable<RaceStateEvent> stateEvents);

    void unscheduleStateEvent(RaceStateEvents stateEventName);

    void unscheduleAllEvents();
}
