package com.sap.sailing.server.operationaltransformation.racelog;

import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogStartTimeEventWithTransformationSupport extends RaceLogEventWithTransformationSupport<RaceLogStartTimeEvent>{
    public RaceLogStartTimeEventWithTransformationSupport(RaceLogStartTimeEvent raceLogEvent) {
        super(raceLogEvent);
    }

    @Override
    public RaceLogEventWithTransformationSupport<?> transformClientOp(RaceLogEventWithTransformationSupport<?> serverOp) {
        return serverOp.transformClientOpForStartTimeEvent(this);
    }

    @Override
    public RaceLogEventWithTransformationSupport<?> transformServerOp(RaceLogEventWithTransformationSupport<?> clientOp) {
        return clientOp.transformServerOpForStartTimeEvent(this);
    }

    /**
     * This is the CLIENT operation, and the SERVER start time event collides with the CLIENT start time event.
     * The server's start time is to be set as the one that is used, and the client's event is to be removed from the log.
     */
    @Override
    public RaceLogEventWithTransformationSupport<?> transformClientOpForStartTimeEvent(
            RaceLogStartTimeEventWithTransformationSupport raceLogStartTimeServerEventWithTransformationSupport) {
        return null;
    }

    @Override
    public boolean requiresSynchronousExecution() {
        return true;
    }
}
