package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sse.common.TimePoint;

public class StartModePrerequisite extends BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    public StartModePrerequisite(FulfillmentFunction function, ConfigurableStartModeFlagRacingProcedure procedure,
            TimePoint originalNow, TimePoint originalStartTime) {
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
        fulfill(getProcedure().getDefaultStartMode());
    }
    
    protected ConfigurableStartModeFlagRacingProcedure getProcedure() {
        return (ConfigurableStartModeFlagRacingProcedure) procedure;
    }
}
