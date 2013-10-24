package com.sap.sailing.racecommittee.app.domain.startprocedure;

import com.sap.sailing.domain.common.TimePoint;

public interface StartProcedureListener {
    void onRaceAborted(TimePoint eventTime);
    
    void onRaceStartphaseEntered(TimePoint eventTime);

    void onRaceStarted(TimePoint eventTime);

    void onRaceFinishing(TimePoint eventTime);
    
    void onRaceFinishing(TimePoint eventTime, TimePoint automaticRaceEnd);
    
    void onRaceFinished(TimePoint eventTime);
    
    void onStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId);
}
