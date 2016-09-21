package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.SWCRacingProcedure;
import com.sap.sse.common.TimePoint;

/**
 * Easy access to the {@link SWCRacingProcedure}.
 */
public abstract class BaseSWCPrerequisite extends BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    public BaseSWCPrerequisite(FulfillmentFunction function, SWCRacingProcedure procedure, TimePoint originalNow,
            TimePoint originalStartTime) {
        super(function, procedure, originalNow, originalStartTime);
    }

    protected SWCRacingProcedure getProcedure() {
        return (SWCRacingProcedure) procedure;
    }

}
