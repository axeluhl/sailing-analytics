package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;

public class GateStartConfigurationImpl extends RacingProcedureConfigurationImpl implements GateStartConfiguration {

    private static final long serialVersionUID = 1862408542829215027L;
    
    private Boolean hasPathfinder;

    public void setHasPathfinder(Boolean hasPathfinder) {
        this.hasPathfinder = hasPathfinder;
    }

    @Override
    public Boolean hasPathfinder() {
        return hasPathfinder;
    }
    
    protected GateStartConfiguration copy() {
        GateStartConfigurationImpl copy = (GateStartConfigurationImpl) super.copy(new GateStartConfigurationImpl());
        copy.setHasPathfinder(hasPathfinder());
        return copy;
    }

}
