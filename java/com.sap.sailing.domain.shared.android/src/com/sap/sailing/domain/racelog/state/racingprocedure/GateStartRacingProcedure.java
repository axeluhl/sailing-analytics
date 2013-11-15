package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;


public interface GateStartRacingProcedure extends RacingProcedure {
    
    public final static long DefaultGolfDownTimeout = 4 * 60 * 1000; // minutes * seconds * milliseconds
    
    void addChangedListener(GateStartChangedListener listener);

    TimePoint getGateLaunchStopTime(TimePoint startTime);
    TimePoint getGateShutdownTime(TimePoint startTime);
    Long getGateLaunchTime();
    void setGateLaunchTime(TimePoint timePoint, long milliseconds);

    String getPathfinder();
    void setPathfinder(TimePoint timePoint, String sailingId);

}
