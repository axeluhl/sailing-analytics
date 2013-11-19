package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.ess.impl.ESSRacingProcedureImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.GateStartRacingProcedureImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl.RRS26RacingProcedureImpl;

public class ReadonlyRacingProcedureFactory implements RacingProcedureFactory {

    private final StoredRacingProceduresConfiguration configuration;
    
    public ReadonlyRacingProcedureFactory(StoredRacingProceduresConfiguration configuration) {
        this.configuration = configuration;
    }
    
    protected RaceLogEventAuthor getAuthor() {
        return null;
    }
    
    protected RaceLogEventFactory getEventFactory() {
        return null;
    }

    @Override
    public ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog) {
        RaceLogEventAuthor author = getAuthor();
        RaceLogEventFactory factory = getEventFactory();
        RacingProceduresConfiguration loadedConfiguration = configuration.load();
        switch (type) {
        case ESS:
            return new ESSRacingProcedureImpl(raceLog, author, factory, loadedConfiguration);
        case GateStart:
            return new GateStartRacingProcedureImpl(raceLog, author, factory, loadedConfiguration);
        case RRS26:
            return new RRS26RacingProcedureImpl(raceLog, author, factory, loadedConfiguration);
        default:
            throw new UnsupportedOperationException("Unknown racing procedure " + type.toString());
        }
    }

}
