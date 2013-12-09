package com.sap.sailing.domain.racelog.state.racingprocedure.ess;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;


public interface ReadonlyESSRacingProcedure extends ReadonlyRacingProcedure {
    
    void addChangedListener(ESSChangedListener listener);

    TimePoint getTimeLimit(TimePoint startTime);

}
