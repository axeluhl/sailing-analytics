package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

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
