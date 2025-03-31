package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
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
    
    @Override
    public RacingProcedureConfiguration merge(RacingProcedureConfiguration value) {
        GateStartConfiguration update = (GateStartConfiguration) value;
        GateStartConfigurationImpl target = (GateStartConfigurationImpl) super.merge(update);
        if (update.hasPathfinder() != null) {
            target.setHasPathfinder(update.hasPathfinder());
        }
        if (update.hasAdditionalGolfDownTime() != null) {
            target.setHasAdditionalGolfDownTime(update.hasAdditionalGolfDownTime());
        }
        return target;
    }
    
    
    @Override
    protected GateStartConfigurationImpl newInstance() {
        return new GateStartConfigurationImpl();
    }

    protected GateStartConfiguration copy() {
        GateStartConfigurationImpl copy = (GateStartConfigurationImpl) super.copy();
        copy.setHasPathfinder(hasPathfinder);
        copy.setHasAdditionalGolfDownTime(hasAdditionalGolfDownTime);
        return copy;
    }
}
