package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;


public interface GateStartRacingProcedure extends RacingProcedure2 {
    
    void addChangedListener(GateStartChangedListener listener);

    TimePoint getGateShutdownTime(TimePoint startTime);
    Long getGateLineOpeningTime();
    void setGateLineOpeningTime(long milliseconds);

}
