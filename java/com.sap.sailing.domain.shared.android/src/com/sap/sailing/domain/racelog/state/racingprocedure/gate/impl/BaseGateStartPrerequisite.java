package com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedurePrerequisite;
import com.sap.sse.common.TimePoint;

/**
 * Easy access to the {@link GateStartRacingProcedure}.
 */
public abstract class BaseGateStartPrerequisite extends BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    public BaseGateStartPrerequisite(FulfillmentFunction function, GateStartRacingProcedure procedure, TimePoint originalNow,
            TimePoint originalStartTime) {
        super(function, procedure, originalNow, originalStartTime);
    }

    protected GateStartRacingProcedure getProcedure() {
        return (GateStartRacingProcedure) procedure;
    }

}
