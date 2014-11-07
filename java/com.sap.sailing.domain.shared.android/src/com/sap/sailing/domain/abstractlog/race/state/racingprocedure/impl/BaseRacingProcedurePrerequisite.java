package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.common.TimePoint;

/**
 * Base class for all {@link RacingProcedurePrerequisite}.
 * 
 * Whenever your implementation of {@link RacingProcedurePrerequisite} is fulfilled call
 * {@link BaseRacingProcedurePrerequisite#fulfilled()} to ensure that the next prerequisites is found and resolved.
 */
public abstract class BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    /**
     * {@link FulfillmentFunction} that is passed to the next {@link RacingProcedurePrerequisite} until all prerequisites are fulfilled.
     */
    protected final FulfillmentFunction function;
    
    /**
     * The {@link RacingProcedure} setting the {@link RacingProcedurePrerequisite}s. 
     */
    protected final RacingProcedure procedure;
    
    /**
     * User-requested {@link TimePoint} depicting the current time.
     */
    protected final TimePoint originalNow;
    
    /**
     * User-requested new start time. 
     */
    protected final TimePoint originalStartTime;

    /**
     * The resolver
     */
    protected Resolver resolver;

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

    /**
     * Implement in your derived type for double dispatch.
     */
    protected abstract void resolveOn(Resolver resolver);

    private RacingProcedurePrerequisite getNextPrerequisite() {
        return procedure.checkPrerequisitesForStart(originalNow, originalStartTime, function);
    }

}
