package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;


public interface ESSRacingProcedure extends RacingProcedure2 {
    
    void addChangedListener(ESSChangedListener listener);

    TimePoint getTimeLimit(TimePoint startTime);

}
