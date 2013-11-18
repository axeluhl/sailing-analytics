package com.sap.sailing.domain.racelog.state.racingprocedure.gate;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;


public interface GateStartRacingProcedure extends ReadonlyGateStartRacingProcedure, RacingProcedure {
    
    void setGateLaunchTime(TimePoint timePoint, long milliseconds);
    void setPathfinder(TimePoint timePoint, String sailingId);

}
