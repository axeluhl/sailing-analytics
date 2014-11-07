package com.sap.sailing.domain.abstractlog.race.state;

public interface RaceStateEventProcessor {
    
    boolean processStateEvent(RaceStateEvent event);

}
