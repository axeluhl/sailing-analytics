package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;

public class StartProcedureTypeAnalyzer extends RaceLogAnalyzer<StartProcedureType> {

    public StartProcedureTypeAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected StartProcedureType performAnalyzation() {
        StartProcedureType result = null;
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogStartProcedureChangedEvent) {
                RaceLogStartProcedureChangedEvent startProcedureEvent = (RaceLogStartProcedureChangedEvent) event;
                result = startProcedureEvent.getStartProcedureType();
            }
        }
        return result;
    }

}
