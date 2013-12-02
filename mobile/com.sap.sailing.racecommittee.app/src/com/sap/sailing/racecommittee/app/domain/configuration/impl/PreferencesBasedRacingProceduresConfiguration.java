package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import java.util.ArrayList;
import java.util.HashSet;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProcedureConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class PreferencesBasedRacingProceduresConfiguration extends RacingProceduresConfigurationImpl implements
        ConfigurationLoader<RacingProceduresConfiguration> {

    private static final long serialVersionUID = -2109422929668306199L;
    private static final String TAG = PreferencesBasedRacingProceduresConfiguration.class.getSimpleName();

    private final AppPreferences preferences;

    public PreferencesBasedRacingProceduresConfiguration(final AppPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public RacingProceduresConfiguration load() {
        RRS26ConfigurationImpl rrs26 = new RRS26ConfigurationImpl();
        rrs26.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.RRS26));
        rrs26.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.RRS26));
        rrs26.setStartModeFlags(new ArrayList<Flags>(preferences.getRRS26StartmodeFlags()));
        setRRS26Configuration(rrs26);

        GateStartConfigurationImpl gateStart = new GateStartConfigurationImpl();
        gateStart.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.GateStart));
        gateStart.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.GateStart));
        gateStart.setHasPathfinder(preferences.getGateStartHasPathfinder());
        gateStart.setHasAdditionalGolfDownTime(preferences.getGateStartHasAdditionalGolfDownTime());
        setGateStartConfiguration(gateStart);

        ESSConfigurationImpl ess = new ESSConfigurationImpl();
        ess.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.ESS));
        ess.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.ESS));
        setESSConfiguration(ess);
        
        RacingProcedureConfigurationImpl basic = new RacingProcedureConfigurationImpl();
        basic.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.BASIC));
        basic.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.BASIC));
        setBasicConfiguration(basic);

        return copy();
    }

    @Override
    public void store() {
        if (!preferences.isRacingProcedureConfigurationOverwriteAllowed()) {
            ExLog.i(TAG, "Overwrite of procedure configuration not allowed.");
            return;
        }
        ExLog.i(TAG, "Storing new racing procedure configuration.");

        if (getRRS26Configuration() != null) {
            RRS26Configuration config = getRRS26Configuration();
            storeRacingProcedureConfiguration(RacingProcedureType.RRS26, config);
            if (config.getStartModeFlags() != null) {
                preferences.setRRS26StartmodeFlags(new HashSet<Flags>(config.getStartModeFlags()));
            }
        }
        if (getGateStartConfiguration() != null) {
            GateStartConfiguration config = getGateStartConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.GateStart, config);
            if (config.hasPathfinder() != null) {
                preferences.setGateStartHasPathfinder(config.hasPathfinder());
            }
            if (config.hasAdditionalGolfDownTime() != null) {
                preferences.setGateStartHasAdditionalGolfDownTime(config.hasAdditionalGolfDownTime());
            }
        }
        if (getESSConfiguration() != null) {
            ESSConfiguration config = getESSConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.ESS, config);
        }
        if (getBasicConfiguration() != null) {
            RacingProcedureConfiguration config = getBasicConfiguration();
            storeRacingProcedureConfiguration(RacingProcedureType.BASIC, config);
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
