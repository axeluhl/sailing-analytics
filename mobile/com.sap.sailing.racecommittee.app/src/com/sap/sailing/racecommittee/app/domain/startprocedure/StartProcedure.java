package com.sap.sailing.racecommittee.app.domain.startprocedure;

import java.util.List;

import com.sap.sailing.domain.common.TimePoint;

public interface StartProcedure {
    
    TimePoint getStartTimeEventTime();
    
    List<TimePoint> getTriggerEventTimePoints(TimePoint startTime);
    
    void dispatchTriggeredEventTimePoint(TimePoint startTime, TimePoint eventTime);
    
    void setRaceStateChangedListener(StartProcedureRaceStateChangedListener raceStateChangedListener);

}
