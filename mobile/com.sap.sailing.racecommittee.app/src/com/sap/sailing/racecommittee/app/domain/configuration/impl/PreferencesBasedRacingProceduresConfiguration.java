package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import java.util.ArrayList;
import java.util.HashSet;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;
import com.sap.sailing.domain.base.configuration.impl.ESSConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.GateStartConfigurationImpl;
import com.sap.sailing.domain.base.configuration.impl.RRS26ConfigurationImpl;
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
    private static final String TAG = PreferencesBasedRacingProceduresConfiguration.class.getName();

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
        setGateStartConfiguration(gateStart);

        ESSConfigurationImpl ess = new ESSConfigurationImpl();
        ess.setClassFlag(preferences.getRacingProcedureClassFlag(RacingProcedureType.ESS));
        ess.setHasInidividualRecall(preferences.getRacingProcedureHasIndividualRecall(RacingProcedureType.ESS));
        setESSConfiguration(ess);

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
            if (config.getClassFlag() != null) {
                preferences.setRacingProcedureClassFlag(RacingProcedureType.RRS26, config.getClassFlag());
            }
            if (config.hasInidividualRecall() != null) {
                preferences.setRacingProcedureHasIndividualRecall(RacingProcedureType.RRS26, config.hasInidividualRecall());
            }
            if (config.getStartModeFlags() != null) {
                preferences.setRRS26StartmodeFlags(new HashSet<Flags>(config.getStartModeFlags()));
            }
        }
        if (getGateStartConfiguration() != null) {
            GateStartConfiguration config = getGateStartConfiguration();
            if (config.getClassFlag() != null) {
                preferences.setRacingProcedureClassFlag(RacingProcedureType.GateStart, config.getClassFlag());
            }
            if (config.hasInidividualRecall() != null) {
                preferences.setRacingProcedureHasIndividualRecall(RacingProcedureType.GateStart, config.hasInidividualRecall());
            }
        }
        if (getESSConfiguration() != null) {
            ESSConfiguration config = getESSConfiguration();
            if (config.getClassFlag() != null) {
                preferences.setRacingProcedureClassFlag(RacingProcedureType.ESS, config.getClassFlag());
            }
            if (config.hasInidividualRecall() != null) {
                preferences.setRacingProcedureHasIndividualRecall(RacingProcedureType.ESS, config.hasInidividualRecall());
            }
        }
    }

}
