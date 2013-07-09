package com.sap.sailing.server.operationaltransformation.racelog;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.operationaltransformation.OperationWithTransformationSupport;

public abstract class RaceLogEventWithTransformationSupport<E extends RaceLogEvent> implements OperationWithTransformationSupport<RaceLog, RaceLogEventWithTransformationSupport<?>> {
    private final E raceLogEvent;
    
    public RaceLogEventWithTransformationSupport(E raceLogEvent) {
        super();
        this.raceLogEvent = raceLogEvent;
    }

    @Override
    public RaceLog applyTo(RaceLog toState) {
        toState.add(raceLogEvent);
        return toState;
    }

    @Override
    public RaceLogEventWithTransformationSupport<E> transformClientOp(RaceLogEventWithTransformationSupport<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceLogEventWithTransformationSupport<E> transformServerOp(RaceLogEventWithTransformationSupport<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
