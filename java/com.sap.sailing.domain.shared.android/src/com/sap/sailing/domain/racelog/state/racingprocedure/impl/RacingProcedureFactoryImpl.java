package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;

public class RacingProcedureFactoryImpl {
    
    public static RacingProcedure create(RacingProcedureType type, RaceLog raceLog,
            RaceLogEventAuthor author, RaceLogEventFactory factory, RacingProceduresConfiguration configuration) {
        switch (type) {
        case ESS:
            return new ESSRacingProcedureImpl(raceLog, author, factory, configuration);
        case GateStart:
            return new GateStartRacingProcedureImpl(raceLog, author, factory, configuration);
        case RRS26:
            return new RRS26RacingProcedureImpl(raceLog, author, factory, configuration);
        default:
            throw new UnsupportedOperationException("Unknown racing procedure " + type.toString());
        }
    }
}
