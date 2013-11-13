package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;

public class RacingProcedureTypeAnalyzer extends RaceLogAnalyzer<RacingProcedureType> {

    public RacingProcedureTypeAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RacingProcedureType performAnalysis() {
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogStartProcedureChangedEvent) {
                RaceLogStartProcedureChangedEvent startProcedureEvent = (RaceLogStartProcedureChangedEvent) event;
                return startProcedureEvent.getStartProcedureType();
            }
        }
        return RacingProcedureType.UNKNOWN;
    }

}
