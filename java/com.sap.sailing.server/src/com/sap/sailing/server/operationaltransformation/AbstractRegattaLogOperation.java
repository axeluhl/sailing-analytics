package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventServiceOperation;

public abstract class AbstractRegattaLogOperation<T> extends AbstractRacingEventServiceOperation<T> {

    private static final long serialVersionUID = 2748436647846774234L;

    public AbstractRegattaLogOperation() {
    }
   
    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }
}