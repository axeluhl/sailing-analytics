package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.impl.NoAddingRaceLogWrapper;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureFactory;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.basic.impl.BasicRacingProcedureImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.ess.impl.ESSRacingProcedureImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.GateStartRacingProcedureImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl.RRS26RacingProcedureImpl;

/**
 * Factory for creating {@link ReadOnlyRacingProcedure}s.
 * 
 * The {@link ReadonlyRacingProcedureFactory} uses a {@link ConfigurationLoader} to ensure that any newly created
 * {@link RacingProcedure} is passed a recent and immutable configuration.
 */
public class ReadonlyRacingProcedureFactory implements RacingProcedureFactory {

    protected final ConfigurationLoader<RegattaConfiguration> configuration;

    public ReadonlyRacingProcedureFactory(ConfigurationLoader<RegattaConfiguration> configuration) {
        this.configuration = configuration;
    }

    @Override
    public RegattaConfiguration getConfiguration() {
        return configuration.load();
    }

    protected ReadonlyRacingProcedure createProcedure(RacingProcedureType type, RaceLog raceLog,
            RaceLogEventAuthor author, RaceLogEventFactory factory) {
        RegattaConfiguration loadedConfiguration = configuration.load();
        switch (type) {
        case ESS:
            return new ESSRacingProcedureImpl(raceLog, author, factory, loadedConfiguration.getESSConfiguration());
        case GateStart:
            return new GateStartRacingProcedureImpl(raceLog, author, factory,
                    loadedConfiguration.getGateStartConfiguration());
        case RRS26:
            return new RRS26RacingProcedureImpl(raceLog, author, factory, loadedConfiguration.getRRS26Configuration());
        case BASIC:
            return new BasicRacingProcedureImpl(raceLog, author, factory, loadedConfiguration.getBasicConfiguration());
        default:
            throw new UnsupportedOperationException("Unknown racing procedure " + type.toString());
        }
    }

    @Override
    public ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog) {
        // Just a mock author since we will never add anything to the racelog
        RaceLogEventAuthor author = new RaceLogEventAuthorImpl("Illegal Author", 128);
        // Wrap the racelog to disable adding...
        RaceLog wrappedRaceLog = new NoAddingRaceLogWrapper(raceLog);
        return createProcedure(type, wrappedRaceLog, author, RaceLogEventFactory.INSTANCE);
    }

}
