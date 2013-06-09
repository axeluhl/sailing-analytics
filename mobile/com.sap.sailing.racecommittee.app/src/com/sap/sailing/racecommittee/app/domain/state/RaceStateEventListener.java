package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.racecommittee.app.services.RaceStateService;

/**
 * Interfaces used to implement communication between {@link RaceState} and {@link RaceStateService}.
 */
public interface RaceStateEventListener {
    
    void onStartTimeChanged(TimePoint startTime);
    
    void onRaceAborted();
    
    void onStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId);
}