package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;

import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;

/**
 * Interface holding configuration options for a Regatta, including configurations for 
 * all types of a {@link RacingProcedure}s.
 */
public interface RegattaConfiguration extends Serializable {
    
    RRS26Configuration getRRS26Configuration();
    GateStartConfiguration getGateStartConfiguration();
    ESSConfiguration getESSConfiguration();
    RacingProcedureConfiguration getBasicConfiguration();

}
