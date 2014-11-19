package com.sap.sailing.domain.racelog.state.racingprocedure.gate;

import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sse.common.TimePoint;


public interface ReadonlyGateStartRacingProcedure extends ReadonlyRacingProcedure {
    
    public final static long DefaultGateLaunchStopTime = 4 * 60 * 1000; // 4 minutes
    public final static long DefaultGolfDownTime = 3 * 60 * 1000; // 3 minutes
    public final static String DefaultPathfinderId = "";
    
    GateStartConfiguration getConfiguration();
    
    void addChangedListener(GateStartChangedListener listener);

    TimePoint getGateLaunchStopTimePoint(TimePoint startTime);
    TimePoint getGateShutdownTimePoint(TimePoint startTime);
    long getGateLaunchStopTime();
    long getGolfDownTime();

    String getPathfinder();

}
