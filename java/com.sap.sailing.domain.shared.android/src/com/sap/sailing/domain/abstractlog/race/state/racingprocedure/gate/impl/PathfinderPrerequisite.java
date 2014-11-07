package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.common.TimePoint;


public class PathfinderPrerequisite extends BaseGateStartPrerequisite implements RacingProcedurePrerequisite {

    public PathfinderPrerequisite(FulfillmentFunction function, GateStartRacingProcedure procedure,
            TimePoint originalNow, TimePoint originalStartTime) {
        super(function, procedure, originalNow, originalStartTime);
    }

    @Override
    public void resolveOn(Resolver resolver) {
        resolver.fulfill(this);
    }
    
    public void fulfill(String pathfinder) {
        getProcedure().setPathfinder(originalNow, pathfinder);
        super.fulfilled();
    }

    @Override
    public void fulfillWithDefault() {
        fulfill(GateStartRacingProcedure.DefaultPathfinderId);
    }
    
}
