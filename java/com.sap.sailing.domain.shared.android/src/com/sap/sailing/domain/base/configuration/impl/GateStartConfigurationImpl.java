package com.sap.sailing.domain.base.configuration.impl;

import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;

public class GateStartConfigurationImpl extends RacingProcedureConfigurationImpl implements GateStartConfiguration {

    private static final long serialVersionUID = 1862408542829215027L;
    
    protected GateStartConfiguration copy() {
        GateStartConfigurationImpl copy = (GateStartConfigurationImpl) super.copy(new GateStartConfigurationImpl());
        return copy;
    }

}
