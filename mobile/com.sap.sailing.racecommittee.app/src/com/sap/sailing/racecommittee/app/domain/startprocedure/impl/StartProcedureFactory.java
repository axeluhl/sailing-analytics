package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import android.content.Context;

import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;

public class StartProcedureFactory {

    public static StartProcedure create(Context context, StartProcedureType type, RaceLog raceLog) {
        switch (type) {
        case ESS:
            return new ExtremeSailingSeriesStartProcedure(context, raceLog);
        case GateStart:
            return new GateStartProcedure(context, raceLog);
        case RRS26:
            return new RRS26StartProcedure(context, raceLog);
        default:
            throw new UnsupportedOperationException("Not yet implemented start procedure.");
        }
    }
}
