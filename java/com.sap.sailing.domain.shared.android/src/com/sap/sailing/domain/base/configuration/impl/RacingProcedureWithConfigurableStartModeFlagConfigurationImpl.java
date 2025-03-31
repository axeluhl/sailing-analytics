package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.ConfigurableStartModeFlagRacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public abstract class RacingProcedureWithConfigurableStartModeFlagConfigurationImpl extends RacingProcedureConfigurationImpl implements ConfigurableStartModeFlagRacingProcedureConfiguration {

    private static final long serialVersionUID = 5141617199229598965L;

    private List<Flags> startModeFlags;

    @Override
    public List<Flags> getStartModeFlags() {
        return startModeFlags;
    }
    
    public void setStartModeFlags(List<Flags> flags) {
        this.startModeFlags = flags;
    }
    
    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration value) {
        ConfigurableStartModeFlagRacingProcedureConfiguration update = (ConfigurableStartModeFlagRacingProcedureConfiguration) value;
        RacingProcedureWithConfigurableStartModeFlagConfigurationImpl target = (RacingProcedureWithConfigurableStartModeFlagConfigurationImpl) super.merge(update);
        if (update.getStartModeFlags() != null) {
            target.setStartModeFlags(update.getStartModeFlags());
        }
        return target;
    }

    protected ConfigurableStartModeFlagRacingProcedureConfiguration copy() {
        RacingProcedureWithConfigurableStartModeFlagConfigurationImpl copy = (RacingProcedureWithConfigurableStartModeFlagConfigurationImpl) super.copy();
        copy.setStartModeFlags(startModeFlags);
        return copy;
    }
}
