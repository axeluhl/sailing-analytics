package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import java.util.ArrayList;
import java.util.HashSet;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppPreferences;

public class PreferencesRegattaConfigurationLoader implements ConfigurationLoader<RegattaConfiguration> {

    private static final String TAG = PreferencesRegattaConfigurationLoader.class.getSimpleName();
    
    public static PreferencesRegattaConfigurationLoader loadFromPreferences(AppPreferences preferences) {
        PreferencesRegattaConfigurationLoader loader = new PreferencesRegattaConfigurationLoader(new RegattaConfigurationImpl(), preferences);
        loader.load();
        return loader;
    }

    private final RegattaConfigurationImpl configuration;
    private final AppPreferences preferences;

    public PreferencesRegattaConfigurationLoader(RegattaConfiguration configuration, AppPreferences preferences) {
        if (!(configuration instanceof RegattaConfigurationImpl)) {
            throw new IllegalArgumentException("configuration");
        }
        this.configuration = (RegattaConfigurationImpl) configuration;
        this.preferences = preferences;
    }

    @Override
    public RegattaConfiguration load() {
        
        configuration.setDefaultRacingProcedureType(preferences.getDefaultRacingProcedureType());
        configuration.setDefaultCourseDesignerMode(preferences.getDefaultCourseDesignerMode());
        
        RRS26ConfigurationImpl rrs26 = new RRS26ConfigurationImpl();
        rrs26.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.RRS26));
        rrs26.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.RRS26));
        rrs26.setStartModeFlags(new ArrayList<Flags>(preferences.getRRS26StartmodeFlags()));
        configuration.setRRS26Configuration(rrs26);

        GateStartConfigurationImpl gateStart = new GateStartConfigurationImpl();
        gateStart.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.GateStart));
        gateStart.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.GateStart));
        gateStart.setHasPathfinder(preferences.getGateStartHasPathfinder());
        gateStart.setHasAdditionalGolfDownTime(preferences.getGateStartHasAdditionalGolfDownTime());
        configuration.setGateStartConfiguration(gateStart);

        ESSConfigurationImpl ess = new ESSConfigurationImpl();
        ess.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.ESS));
        ess.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.ESS));
        configuration.setESSConfiguration(ess);
        
        RacingProcedureConfigurationImpl basic = new RacingProcedureConfigurationImpl();
        basic.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.BASIC));
        basic.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.BASIC));
        configuration.setBasicConfiguration(basic);

        return configuration.clone();
    }

    @Override
    public void store() {
        ExLog.i(preferences.getContext(), TAG, "Storing new racing procedure configuration.");
        
        if (configuration.getDefaultRacingProcedureType() != null) {
            preferences.setDefaultRacingProcedureType(configuration.getDefaultRacingProcedureType());
        }
        
        if (configuration.getDefaultCourseDesignerMode() != null) {
            preferences.setDefaultCourseDesignerMode(configuration.getDefaultCourseDesignerMode());
        }

        if (configuration.getRRS26Configuration() != null) {
            RRS26Configuration config = configuration.getRRS26Configuration();
            storeRacingProcedureConfiguration(RacingProcedureType.RRS26, config);
            if (config.getStartModeFlags() != null) {
                preferences.setRRS26StartmodeFlags(new HashSet<Flags>(config.getStartModeFlags()));
            }
        }
        if (configuration.getGateStartConfiguration() != null) {
            GateStartConfiguration config = configuration.getGateStartConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.GateStart, config);
            if (config.hasPathfinder() != null) {
                preferences.setGateStartHasPathfinder(config.hasPathfinder());
            }
            if (config.hasAdditionalGolfDownTime() != null) {
                preferences.setGateStartHasAdditionalGolfDownTime(config.hasAdditionalGolfDownTime());
            }
        }
        if (configuration.getESSConfiguration() != null) {
            ESSConfiguration config = configuration.getESSConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.ESS, config);
        }
        if (configuration.getBasicConfiguration() != null) {
            RacingProcedureConfiguration config = configuration.getBasicConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.BASIC, config);
        }
        if (configuration.getLeagueConfiguration() != null) {
            LeagueConfiguration config = configuration.getLeagueConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.LEAGUE, config);
        }
    }

    private void storeRacingProcedureConfiguration(RacingProcedureType type, RacingProcedureConfiguration config) {
        if (config.getClassFlag() != null) {
            preferences.setRacingProcedureClassFlag(type, config.getClassFlag());
        }
        if (config.hasInidividualRecall() != null) {
            preferences.setRacingProcedureHasIndividualRecall(type, config.hasInidividualRecall());
        }
    }

}
