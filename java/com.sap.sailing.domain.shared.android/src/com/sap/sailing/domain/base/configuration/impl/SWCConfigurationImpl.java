package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.SWCConfiguration;

public class SWCConfigurationImpl extends RacingProcedureWithConfigurableStartModeFlagConfigurationImpl
        implements SWCConfiguration {

    private static final long serialVersionUID = 5141617199229598965L;

    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration value) {
        SWCConfiguration update = (SWCConfiguration) value;
        SWCConfigurationImpl target = (SWCConfigurationImpl) super.merge(update);
        if (update.getStartModeFlags() != null) {
            target.setStartModeFlags(update.getStartModeFlags());
        }
        return target;
    }
}
