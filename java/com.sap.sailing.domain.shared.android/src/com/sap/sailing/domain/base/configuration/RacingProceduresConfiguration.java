package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;

/**
 * Aggregate interface holding configuration options for all {@link RacingProcedure}s.
 */
public interface RacingProceduresConfiguration extends Serializable {
    
    RRS26Configuration getRRS26Configuration();
    GateStartConfiguration getGateStartConfiguration();
    ESSConfiguration getESSConfiguration();

}
