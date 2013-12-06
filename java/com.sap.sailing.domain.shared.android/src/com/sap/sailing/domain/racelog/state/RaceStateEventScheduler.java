package com.sap.sailing.domain.racelog.state;

import java.util.Collection;

import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;

public interface RaceStateEventScheduler {
 
    void scheduleStateEvents(Collection<RaceStateEvent> stateEvents);

    void unscheduleStateEvent(RaceStateEvents stateEventName);

    void unscheduleAllEvents();
    
    
}
