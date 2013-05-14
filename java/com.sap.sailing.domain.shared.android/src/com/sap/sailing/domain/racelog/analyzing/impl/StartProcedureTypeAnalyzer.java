package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartProcedureChangedEvent;

public class StartProcedureTypeAnalyzer extends RaceLogAnalyzer {

    public StartProcedureTypeAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }
    
    public StartProcedureType getActiveStartProcedureType() {
        StartProcedureType result = null;
        raceLog.lockForRead();
        try {
            result = searchLog();
        } finally {
            raceLog.unlockAfterRead();
        }
        return result;
    }

    private StartProcedureType searchLog() {
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
