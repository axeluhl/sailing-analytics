package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.SWCConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public class SWCConfigurationImpl extends RacingProcedureConfigurationImpl implements
    SWCConfiguration {

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
        SWCConfiguration update = (SWCConfiguration) value;
        SWCConfigurationImpl target = (SWCConfigurationImpl) super.merge(update);
        if (update.getStartModeFlags() != null) {
            target.setStartModeFlags(update.getStartModeFlags());
        }
        return target;
    }

    protected SWCConfiguration copy() {
        SWCConfigurationImpl copy = (SWCConfigurationImpl) super.copy(new SWCConfigurationImpl());
        copy.setStartModeFlags(startModeFlags);
        return copy;
    }

}
