package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.common.TimePoint;

public class GateLaunchTimePrerequisite extends BaseGateStartPrerequisite implements RacingProcedurePrerequisite {

    private final long golfDownTime;
    
    public GateLaunchTimePrerequisite(FulfillmentFunction function, GateStartRacingProcedure procedure, TimePoint originalNow,
            TimePoint originalStartTime, long golfDownTime) {
        super(function, procedure, originalNow, originalStartTime);
        this.golfDownTime = golfDownTime;
    }
    
    @Override
    protected void resolveOn(Resolver resolver) {
        resolver.fulfill(this);
    }
    
    public void fulfill(long gateLaunchTime, long golfDownTime) {
        getProcedure().setGateLineOpeningTimes(originalNow, gateLaunchTime, golfDownTime);
        super.fulfilled();
    }

    @Override
    public void fulfillWithDefault() {
        fulfill(GateStartRacingProcedure.DefaultGateLaunchStopTime, golfDownTime);
    }

}
