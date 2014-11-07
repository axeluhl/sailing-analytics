package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;

public class StartmodePrerequisite extends BaseRRS26Prerequisite implements RacingProcedurePrerequisite {

    public StartmodePrerequisite(FulfillmentFunction function, RRS26RacingProcedure procedure, TimePoint originalNow,
            TimePoint originalStartTime) {
        super(function, procedure, originalNow, originalStartTime);
    }

    @Override
    protected void resolveOn(Resolver resolver) {
        resolver.fulfill(this);
    }
    
    public void fulfill(Flags startmodeFlag) {
        getProcedure().setStartModeFlag(originalNow, startmodeFlag);
        super.fulfilled();
    }
    
    @Override
    public void fulfillWithDefault() {
        fulfill(RRS26RacingProcedure.DefaultStartMode);
    }

}
