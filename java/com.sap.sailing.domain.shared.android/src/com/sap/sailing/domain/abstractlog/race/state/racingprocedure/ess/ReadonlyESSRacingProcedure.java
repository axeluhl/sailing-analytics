package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sse.common.TimePoint;


public interface ReadonlyESSRacingProcedure extends ReadonlyRacingProcedure {
    
    void addChangedListener(ESSChangedListener listener);

    TimePoint getTimeLimit(TimePoint startTime);

}
