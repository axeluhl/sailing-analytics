package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;

public class ManagedRaceHelper {

    private RaceLog raceLog;
    private AbstractLogEventAuthor author;
    private ConfigurationLoader<RegattaConfiguration> configuration;

    public ManagedRaceHelper(RaceLog raceLog, AbstractLogEventAuthor author, ConfigurationLoader<RegattaConfiguration> configuration) {
        this.raceLog = raceLog;
        this.author = author;
        this.configuration = configuration;
    }

    public RaceLog getRaceLog() {
        return raceLog;
    }

    public AbstractLogEventAuthor getAuthor() {
        return author;
    }

    public ConfigurationLoader<RegattaConfiguration> getConfiguration() {
        return configuration;
    }
}
