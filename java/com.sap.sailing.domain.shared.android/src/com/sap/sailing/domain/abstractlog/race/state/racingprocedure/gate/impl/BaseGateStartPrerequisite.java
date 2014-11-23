package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedurePrerequisite;
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
