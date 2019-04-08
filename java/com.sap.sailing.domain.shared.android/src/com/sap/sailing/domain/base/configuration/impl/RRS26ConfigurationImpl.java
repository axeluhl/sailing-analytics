package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;

public class RRS26ConfigurationImpl extends RacingProcedureWithConfigurableStartModeFlagConfigurationImpl
        implements RRS26Configuration {

    private static final long serialVersionUID = 260941807346644026L;
    
    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration value) {
        RRS26Configuration update = (RRS26Configuration) value;
        RRS26ConfigurationImpl target = (RRS26ConfigurationImpl) super.merge(update);
        if (update.getStartModeFlags() != null) {
            target.setStartModeFlags(update.getStartModeFlags());
        }
        return target;
    }

    protected RRS26ConfigurationImpl newInstance() {
        return new RRS26ConfigurationImpl();
    }

}
