package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;

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
        setStartTime();
    }

    @Override
    public void fulfillWithDefault() {
        setStartTime();
    }

    private void setStartTime() {
        function.execute();
    }

    @Override
    protected void resolveOn(Resolver resolver) {
        throw new UnsupportedOperationException("There should be no prerequisites after me!");
    }

}
