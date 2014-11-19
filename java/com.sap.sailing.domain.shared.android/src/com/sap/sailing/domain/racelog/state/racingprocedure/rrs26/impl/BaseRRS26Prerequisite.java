package com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sse.common.TimePoint;

/**
 * Easy access to the {@link RRS26RacingProcedure}.
 */
public abstract class BaseRRS26Prerequisite extends BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    public BaseRRS26Prerequisite(FulfillmentFunction function, RRS26RacingProcedure procedure, TimePoint originalNow,
            TimePoint originalStartTime) {
        super(function, procedure, originalNow, originalStartTime);
    }

    protected RRS26RacingProcedure getProcedure() {
        return (RRS26RacingProcedure) procedure;
    }

}
