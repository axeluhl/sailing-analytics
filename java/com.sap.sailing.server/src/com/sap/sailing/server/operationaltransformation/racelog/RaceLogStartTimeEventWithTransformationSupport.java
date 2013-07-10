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
}
