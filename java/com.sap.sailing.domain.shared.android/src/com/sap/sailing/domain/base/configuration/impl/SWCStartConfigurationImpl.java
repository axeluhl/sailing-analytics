package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;

public class SWCStartConfigurationImpl extends RacingProcedureWithConfigurableStartModeFlagConfigurationImpl
        implements SWCStartConfiguration {

    private static final long serialVersionUID = 5141617199229598965L;

    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration value) {
        SWCStartConfiguration update = (SWCStartConfiguration) value;
        SWCStartConfigurationImpl target = (SWCStartConfigurationImpl) super.merge(update);
        if (update.getStartModeFlags() != null) {
            target.setStartModeFlags(update.getStartModeFlags());
        }
        return target;
    }
    
    protected SWCStartConfigurationImpl newInstance() {
        return new SWCStartConfigurationImpl();
    }
}
