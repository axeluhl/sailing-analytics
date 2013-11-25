package com.sap.sailing.domain.racelog.state.racingprocedure.gate;

import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;


public interface ReadonlyGateStartRacingProcedure extends ReadonlyRacingProcedure {
    
    public final static long DefaultGolfDownTimeout = 4 * 60 * 1000; // minutes * seconds * milliseconds
    
    GateStartConfiguration getConfiguration();
    
    void addChangedListener(GateStartChangedListener listener);

    TimePoint getGateLaunchStopTime(TimePoint startTime);
    TimePoint getGateShutdownTime(TimePoint startTime);
    Long getGateLaunchTime();

    String getPathfinder();

}
