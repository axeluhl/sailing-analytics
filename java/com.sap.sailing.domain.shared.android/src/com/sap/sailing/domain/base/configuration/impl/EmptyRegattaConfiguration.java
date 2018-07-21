package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;

/**
 * Is empty, does nothing on store and load.
 */
public class EmptyRegattaConfiguration extends RegattaConfigurationImpl implements
        ConfigurationLoader<RegattaConfiguration> {

    private static final long serialVersionUID = -4187341706420504456L;

    @Override
    public RegattaConfiguration load() {
        setDefaultRacingProcedureType(RacingProcedureType.UNKNOWN);
        setDefaultCourseDesignerMode(CourseDesignerMode.UNKNOWN);
        setDefaultProtestTimeDuration(Duration.ONE_MINUTE.times(90));
        setRRS26Configuration(new RRS26ConfigurationImpl());
        setSWCStartConfiguration(new SWCStartConfigurationImpl());
        setGateStartConfiguration(new GateStartConfigurationImpl());
        setESSConfiguration(new ESSConfigurationImpl());
        setBasicConfiguration(new RacingProcedureConfigurationImpl());
        setLeagueConfiguration(new LeagueConfigurationImpl());
        return clone();
    }

    @Override
    public void store() {
        
    }

}
