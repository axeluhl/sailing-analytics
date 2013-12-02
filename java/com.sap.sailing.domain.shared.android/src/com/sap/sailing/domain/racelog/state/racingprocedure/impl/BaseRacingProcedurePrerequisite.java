package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;

public abstract class BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    protected final FulfillmentFunction function;
    protected final RacingProcedure procedure;
    protected final TimePoint originalNow;
    protected final TimePoint originalStartTime;

    private Resolver resolver;

    public BaseRacingProcedurePrerequisite(FulfillmentFunction function, RacingProcedure procedure,
            TimePoint originalNow, TimePoint originalStartTime) {
        this.function = function;
        this.procedure = procedure;
        this.originalNow = originalNow;
        this.originalStartTime = originalStartTime;
    }

    /**
     * Call on fulfillment of your prerequisite. Will continue with next prerequisite!
     */
    protected void fulfilled() {
        RacingProcedurePrerequisite next = getNextPrerequisite();
        next.resolve(resolver);
    }
    
    @Override
    public void resolve(Resolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException("resolver must not be null");
        }
        
        this.resolver = resolver;
        resolveOn(resolver);
    }


    protected abstract void resolveOn(Resolver resolver);

    private RacingProcedurePrerequisite getNextPrerequisite() {
        return procedure.checkPrerequisitesForStart(originalNow, originalStartTime, function);
    }

}
