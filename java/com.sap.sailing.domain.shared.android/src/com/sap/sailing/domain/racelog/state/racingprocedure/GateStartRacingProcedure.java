package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;


public interface GateStartRacingProcedure extends RacingProcedure {
    
    public final static long DefaultGolfDownTimeout = 4 * 60 * 1000; // minutes * seconds * milliseconds
    
    void addChangedListener(GateStartChangedListener listener);

    TimePoint getGateShutdownTime(TimePoint startTime);
    Long getGateLineOpeningTime();
    void setGateLineOpeningTime(TimePoint timePoint, long milliseconds);

    String getPathfinder();
    void setPathfinder(TimePoint timePoint, String sailingId);

}
