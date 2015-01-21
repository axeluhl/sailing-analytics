package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartProcedureChangedEvent;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

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
