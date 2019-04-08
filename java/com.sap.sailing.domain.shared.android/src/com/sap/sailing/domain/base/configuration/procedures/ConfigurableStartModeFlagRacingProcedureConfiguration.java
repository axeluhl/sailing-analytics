package com.sap.sailing.domain.base.configuration.procedures;

import java.util.List;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface ConfigurableStartModeFlagRacingProcedureConfiguration extends RacingProcedureConfiguration {
    
    List<Flags> getStartModeFlags();
}
