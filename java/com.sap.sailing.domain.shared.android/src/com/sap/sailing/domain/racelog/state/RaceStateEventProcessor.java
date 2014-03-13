package com.sap.sailing.domain.racelog.state;

public interface RaceStateEventProcessor {
    
    boolean processStateEvent(RaceStateEvent event);

}
