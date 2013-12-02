package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;

public class GateStartConfigurationImpl extends RacingProcedureConfigurationImpl implements GateStartConfiguration {

    private static final long serialVersionUID = 1862408542829215027L;
    
    private Boolean hasPathfinder;
    private Boolean hasAdditionalGolfDownTime;

    public void setHasPathfinder(Boolean hasPathfinder) {
        this.hasPathfinder = hasPathfinder;
    }

    @Override
    public Boolean hasPathfinder() {
        return hasPathfinder;
    }
    
    public void setHasAdditionalGolfDownTime(Boolean hasAdditionalGolfDownTime) {
        this.hasAdditionalGolfDownTime = hasAdditionalGolfDownTime;
    }

    @Override
    public Boolean hasAdditionalGolfDownTime() {
        return hasAdditionalGolfDownTime;
    }
    
    protected GateStartConfiguration copy() {
        GateStartConfigurationImpl copy = (GateStartConfigurationImpl) super.copy(new GateStartConfigurationImpl());
        copy.setHasPathfinder(hasPathfinder);
        copy.setHasAdditionalGolfDownTime(hasAdditionalGolfDownTime);
        return copy;
    }

}
