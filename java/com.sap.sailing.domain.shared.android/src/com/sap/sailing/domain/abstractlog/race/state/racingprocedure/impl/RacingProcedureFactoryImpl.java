package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class RacingProcedureFactoryImpl extends ReadonlyRacingProcedureFactory implements RacingProcedureFactory {

    private final AbstractLogEventAuthor author;
    
    public RacingProcedureFactoryImpl(AbstractLogEventAuthor author, 
            ConfigurationLoader<RegattaConfiguration> configuration) {
        super(configuration);
        this.author = author;
    }
    
    @Override
    public ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog, RaceLogResolver raceLogResolver) {
        return createProcedure(type, raceLog, author, raceLogResolver);
    }
}
