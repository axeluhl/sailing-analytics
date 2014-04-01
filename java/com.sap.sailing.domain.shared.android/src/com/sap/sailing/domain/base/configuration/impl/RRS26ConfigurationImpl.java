package com.sap.sailing.domain.base.configuration.impl;

import java.util.List;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;

public class RRS26ConfigurationImpl extends RacingProcedureConfigurationImpl implements
        RRS26Configuration {

    private static final long serialVersionUID = 260941807346644026L;
    
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
        RRS26Configuration update = (RRS26Configuration) value;
        RRS26ConfigurationImpl target = (RRS26ConfigurationImpl) super.merge(update);
        if (update.getStartModeFlags() != null) {
            target.setStartModeFlags(update.getStartModeFlags());
        }
        return target;
    }

    protected RRS26Configuration copy() {
        RRS26ConfigurationImpl copy = (RRS26ConfigurationImpl) super.copy(new RRS26ConfigurationImpl());
        copy.setStartModeFlags(startModeFlags);
        return copy;
    }

}
