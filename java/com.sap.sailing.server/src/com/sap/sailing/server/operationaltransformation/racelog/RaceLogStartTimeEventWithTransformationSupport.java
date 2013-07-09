package com.sap.sailing.server.operationaltransformation.racelog;

import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;

public class RaceLogStartTimeEventWithTransformationSupport extends RaceLogEventWithTransformationSupport<RaceLogStartTimeEvent>{
    public RaceLogStartTimeEventWithTransformationSupport(RaceLogStartTimeEvent raceLogEvent) {
        super(raceLogEvent);
    }
}
