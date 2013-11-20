package com.sap.sailing.racecommittee.app.domain.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.StoreableConfiguration;
import com.sap.sailing.domain.base.configuration.impl.RacingProceduresConfigurationImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;

public class PreferencesBasedRacingProceduresConfiguration extends RacingProceduresConfigurationImpl implements
    StoreableConfiguration<RacingProceduresConfiguration> {

    private static final long serialVersionUID = -2109422929668306199L;

    @SuppressWarnings("unused")
    private final AppPreferences preferences;

    public PreferencesBasedRacingProceduresConfiguration(final AppPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public RacingProceduresConfiguration load() {
        return super.copy(this);
    }

    @Override
    public void store() {
        
    }

}
