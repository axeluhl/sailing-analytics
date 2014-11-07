package com.sap.sailing.server.operationaltransformation.racelog;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sse.operationaltransformation.OperationWithTransformationSupport;

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

    /**
     * By default, setting the start time on a CLIENT is not transformed by any SERVER operation for execution on the SERVER.
     */
    public RaceLogEventWithTransformationSupport<?> transformClientOpForStartTimeEvent(
            RaceLogStartTimeEventWithTransformationSupport raceLogStartTimeClientEventWithTransformationSupport) {
        return this;
    }

    /**
     * By default, setting the start time on the SERVER is not transformed by any CLIENT operation for execution on the CLIENT.
     */
    public RaceLogEventWithTransformationSupport<?> transformServerOpForStartTimeEvent(
            RaceLogStartTimeEventWithTransformationSupport raceLogStartTimeServerEventWithTransformationSupport) {
        return this;
    }
}
