package com.sap.sailing.domain.base.configuration.procedures;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;

public interface GateStartConfiguration extends RacingProcedureConfiguration {

    Boolean hasPathfinder();
    Boolean hasAdditionalGolfDownTime();
    
}
