package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import java.util.ArrayList;
import java.util.HashSet;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.LeagueConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RegattaConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.SWCStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sse.common.Duration;

public class PreferencesRegattaConfigurationLoader implements ConfigurationLoader<RegattaConfiguration> {

    private static final String TAG = PreferencesRegattaConfigurationLoader.class.getSimpleName();

    public static PreferencesRegattaConfigurationLoader loadFromPreferences(AppPreferences preferences) {
        PreferencesRegattaConfigurationLoader loader = new PreferencesRegattaConfigurationLoader(
                new RegattaConfigurationImpl(), preferences);
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
        configuration.setDefaultProtestTimeDuration(
                Duration.ONE_MINUTE.times(preferences.getProtestTimeDurationInMinutes()));

        RRS26ConfigurationImpl rrs26 = new RRS26ConfigurationImpl();
        rrs26.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.RRS26));
        rrs26.setHasIndividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.RRS26));
        rrs26.setResultEntryEnabled(preferences.getRacingProcedureIsResultEntryEnabled(RacingProcedureType.RRS26));
        rrs26.setStartModeFlags(new ArrayList<>(preferences.getRRS26StartmodeFlags()));
        configuration.setRRS26Configuration(rrs26);

        SWCStartConfigurationImpl swcStart = new SWCStartConfigurationImpl();
        swcStart.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.SWC));
        swcStart.setHasIndividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.SWC));
        swcStart.setResultEntryEnabled(preferences.getRacingProcedureIsResultEntryEnabled(RacingProcedureType.SWC));
        swcStart.setStartModeFlags(new ArrayList<>(preferences.getSWCStartmodeFlags()));
        configuration.setSWCStartConfiguration(swcStart);

        GateStartConfigurationImpl gateStart = new GateStartConfigurationImpl();
        gateStart.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.GateStart));
        gateStart.setHasIndividualRecall(
                preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.GateStart));
        gateStart.setResultEntryEnabled(
                preferences.getRacingProcedureIsResultEntryEnabled(RacingProcedureType.GateStart));
        gateStart.setHasPathfinder(preferences.getGateStartHasPathfinder());
        gateStart.setHasAdditionalGolfDownTime(preferences.getGateStartHasAdditionalGolfDownTime());
        configuration.setGateStartConfiguration(gateStart);

        ESSConfigurationImpl ess = new ESSConfigurationImpl();
        ess.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.ESS));
        ess.setHasIndividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.ESS));
        ess.setResultEntryEnabled(preferences.getRacingProcedureIsResultEntryEnabled(RacingProcedureType.ESS));
        configuration.setESSConfiguration(ess);

        RacingProcedureConfigurationImpl basic = new RacingProcedureConfigurationImpl();
        basic.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.BASIC));
        basic.setHasIndividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.BASIC));
        basic.setResultEntryEnabled(preferences.getRacingProcedureIsResultEntryEnabled(RacingProcedureType.BASIC));
        configuration.setBasicConfiguration(basic);

        LeagueConfigurationImpl league = new LeagueConfigurationImpl();
        league.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.LEAGUE));
        league.setHasIndividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.LEAGUE));
        league.setResultEntryEnabled(preferences.getRacingProcedureIsResultEntryEnabled(RacingProcedureType.LEAGUE));
        configuration.setLeagueConfiguration(league);

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

        if (configuration.getDefaultProtestTimeDuration() != null) {
            preferences.setDefaultProtestTimeDurationInMinutes(
                    (int) configuration.getDefaultProtestTimeDuration().asMinutes());
            preferences.setDefaultProtestTimeDurationInMinutesCustomEditable(false);
        } else {
            preferences.setDefaultProtestTimeDurationInMinutesCustomEditable(true);
        }

        if (configuration.getRRS26Configuration() != null) {
            RRS26Configuration config = configuration.getRRS26Configuration();
            storeRacingProcedureConfiguration(RacingProcedureType.RRS26, config);
            if (config.getStartModeFlags() != null) {
                preferences.setRRS26StartmodeFlags(new HashSet<>(config.getStartModeFlags()));
            }
        }
        if (configuration.getSWCStartConfiguration() != null) {
            SWCStartConfiguration config = configuration.getSWCStartConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.SWC, config);
            if (config.getStartModeFlags() != null) {
                preferences.setSWCStartmodeFlags(new HashSet<>(config.getStartModeFlags()));
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
        if (config.hasIndividualRecall() != null) {
            preferences.setRacingProcedureHasIndividualRecall(type, config.hasIndividualRecall());
        }
        if (config.isResultEntryEnabled() != null) {
            preferences.setRacingProcedureIsResultEntryEnabled(type, config.isResultEntryEnabled());
        }
    }

}
