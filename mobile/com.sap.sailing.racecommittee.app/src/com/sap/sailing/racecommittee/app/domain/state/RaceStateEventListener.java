package com.sap.sailing.racecommittee.app.domain.state;

import com.sap.sailing.domain.common.TimePoint;

public interface RaceStateEventListener {
    void onStartTimeChanged(TimePoint startTime);
    
    void onRaceAborted();
    
    void onStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId);
}