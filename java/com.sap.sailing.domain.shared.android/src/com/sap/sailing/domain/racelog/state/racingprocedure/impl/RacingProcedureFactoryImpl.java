package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;

public class RacingProcedureFactoryImpl extends ReadonlyRacingProcedureFactory implements RacingProcedureFactory {

    private final RaceLogEventAuthor author;
    private final RaceLogEventFactory factory;
    
    public RacingProcedureFactoryImpl(RaceLogEventAuthor author, RaceLogEventFactory factory, 
            ConfigurationLoader<RegattaConfiguration> configuration) {
        super(configuration);
        this.author = author;
        this.factory = factory;
    }
    
    @Override
    public ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog) {
        return createProcedure(type, raceLog, author, factory);
    }
}
