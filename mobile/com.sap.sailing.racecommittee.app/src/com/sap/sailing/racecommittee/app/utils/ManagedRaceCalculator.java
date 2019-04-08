package com.sap.sailing.racecommittee.app.utils;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateImpl;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.racecommittee.app.data.AndroidRaceLogResolver;

public class ManagedRaceCalculator {

    private RaceLog raceLog;
    private AbstractLogEventAuthor author;
    private ConfigurationLoader<RegattaConfiguration> configuration;

    public ManagedRaceCalculator(RaceLog raceLog, AbstractLogEventAuthor author,
            ConfigurationLoader<RegattaConfiguration> configuration) {
        this.raceLog = raceLog;
        this.author = author;
        this.configuration = configuration;
    }

    public RaceState calculateRaceState() {
        return RaceStateImpl.create(new AndroidRaceLogResolver(), raceLog, author, configuration);
    }
}
