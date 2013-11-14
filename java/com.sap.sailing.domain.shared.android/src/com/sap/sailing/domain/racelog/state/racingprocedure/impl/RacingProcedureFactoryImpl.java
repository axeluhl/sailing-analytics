package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;

public class RacingProcedureFactoryImpl {
    
    public static RacingProcedure create(RacingProcedureType type, RaceLog raceLog,
            RaceLogEventAuthor author, RaceLogEventFactory factory) {
        switch (type) {
        case ESS:
            return new ESSRacingProcedureImpl(raceLog, author, factory);
        case GateStart:
            return new GateStartRacingProcedureImpl(raceLog, author, factory);
        case RRS26:
            return new RRS26RacingProcedureImpl(raceLog, author, factory);
        default:
            throw new UnsupportedOperationException("Unknown racing procedure " + type.toString());
        }
    }
}
