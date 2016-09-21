package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc.SWCRacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sse.common.TimePoint;

public class SWCStartmodePrerequisite extends BaseSWCPrerequisite implements RacingProcedurePrerequisite {

    public SWCStartmodePrerequisite(FulfillmentFunction function, SWCRacingProcedure procedure, TimePoint originalNow,
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
        fulfill(SWCRacingProcedure.DefaultStartMode);
    }

}
