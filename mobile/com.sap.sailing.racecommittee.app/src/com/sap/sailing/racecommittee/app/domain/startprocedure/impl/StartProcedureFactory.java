package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;

public class StartProcedureFactory {

    public static StartProcedure create(StartProcedureType type, PassAwareRaceLog raceLog) {
        switch (type) {
        case ESS:
            return new ExtremeSailingSeriesStartProcedure(raceLog);
        case GateStart:
            return new GateStartProcedure(raceLog);
        default:
            throw new UnsupportedOperationException("Not yet implemented start procedure.");
        }
    }
}
