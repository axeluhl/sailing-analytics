package com.sap.sailing.domain.racelog.state.racingprocedure.gate;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sse.common.TimePoint;


public interface GateStartRacingProcedure extends ReadonlyGateStartRacingProcedure, RacingProcedure {
    
    void setGateLineOpeningTimes(TimePoint now, long gateLaunchTime, long golfDownInterval);
    void setPathfinder(TimePoint timePoint, String sailingId);

}
