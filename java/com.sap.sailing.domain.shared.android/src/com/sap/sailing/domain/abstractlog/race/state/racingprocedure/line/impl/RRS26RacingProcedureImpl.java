package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;

public class RRS26RacingProcedureImpl extends BaseRRS26RacingProcedureImpl {

    private final static Duration startPhaseClassUpInterval = Duration.ONE_MINUTE.times(5);
    private final static Duration startPhaseStartModeUpInterval = Duration.ONE_MINUTE.times(4);

    public RRS26RacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author,
                                    RRS26Configuration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver, startPhaseClassUpInterval, startPhaseStartModeUpInterval);
    }
    
    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.RRS26;
    }
}
