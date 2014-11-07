package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.TimePoint;


public interface GateStartRacingProcedure extends ReadonlyGateStartRacingProcedure, RacingProcedure {
    
    void setGateLineOpeningTimes(TimePoint now, long gateLaunchTime, long golfDownInterval);
    void setPathfinder(TimePoint timePoint, String sailingId);

}
