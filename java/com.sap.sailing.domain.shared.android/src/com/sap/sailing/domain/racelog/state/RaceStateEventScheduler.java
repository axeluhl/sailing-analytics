package com.sap.sailing.domain.racelog.state;

import java.util.Collection;

public interface RaceStateEventScheduler {
 
    void scheduleStateEvents(Collection<RaceStateEvent> stateEvents);

    void unscheduleStateEvent(RaceStateEvent stateEvent);

    void unscheduleAllEvents();
    
    
}
