package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.ConfigurationLoader;

/**
 * Is empty, does nothing on store and load.
 */
public class EmptyRacingProceduresConfiguration extends RacingProceduresConfigurationImpl implements
        ConfigurationLoader<RacingProceduresConfiguration> {

    private static final long serialVersionUID = -4187341706420504456L;

    @Override
    public RacingProceduresConfiguration load() {
        setRRS26Configuration(new RRS26ConfigurationImpl());
        setGateStartConfiguration(new GateStartConfigurationImpl());
        setESSConfiguration(new ESSConfigurationImpl());
        return copy();
    }

    @Override
    public void store() {
        
    }

}
