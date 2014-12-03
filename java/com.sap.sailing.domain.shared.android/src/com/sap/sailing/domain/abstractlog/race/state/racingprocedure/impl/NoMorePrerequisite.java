package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;

/**
 * This special {@link RacingProcedurePrerequisite} won't check for next prerequisites. It just executes the
 * {@link FulfillmentFunction}.
 */
public class NoMorePrerequisite extends BaseRacingProcedurePrerequisite implements RacingProcedurePrerequisite {

    public NoMorePrerequisite(FulfillmentFunction function) {
        super(function, null, null, null);
    }

    @Override
    public void resolve(Resolver resolver) {
        this.resolver = resolver;
        setStartTime();
    }

    @Override
    public void fulfillWithDefault() {
        throw new UnsupportedOperationException("You should not be able to cal me!");
    }

    private void setStartTime() {
        function.execute();
        resolver.onFulfilled();
    }

    @Override
    protected void resolveOn(Resolver resolver) {
        throw new UnsupportedOperationException("There should be no prerequisites after me!");
    }

}
