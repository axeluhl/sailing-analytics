package com.sap.sailing.domain.racelog.state.racingprocedure.ess;

import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sse.common.TimePoint;


public interface ReadonlyESSRacingProcedure extends ReadonlyRacingProcedure {
    
    void addChangedListener(ESSChangedListener listener);

    TimePoint getTimeLimit(TimePoint startTime);

}
