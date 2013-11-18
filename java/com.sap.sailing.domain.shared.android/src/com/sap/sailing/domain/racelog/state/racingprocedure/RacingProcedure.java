package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;

public interface RacingProcedure extends ReadonlyRacingProcedure {
    
    void displayIndividualRecall(TimePoint timePoint);
    void removeIndividualRecall(TimePoint timePoint);

}
