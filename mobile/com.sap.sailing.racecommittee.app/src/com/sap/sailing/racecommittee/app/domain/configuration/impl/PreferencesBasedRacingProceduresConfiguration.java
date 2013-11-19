package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.StoredRacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;

public class PreferencesBasedRacingProceduresConfiguration extends RacingProceduresConfigurationImpl implements
        StoredRacingProceduresConfiguration {

    private static final long serialVersionUID = -2109422929668306199L;

    private final AppPreferences preferences;

    public PreferencesBasedRacingProceduresConfiguration(final AppPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public StoredRacingProceduresConfiguration load() {
        preferences.getAndroidIdentifier();
        return this;
    }

    @Override
    public StoredRacingProceduresConfiguration store() {
        return this;
    }

}
