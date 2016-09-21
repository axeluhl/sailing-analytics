package com.sap.sailing.domain.base.configuration.procedures;

import java.util.List;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

/**
 * Start procedure for the Sailing World Cup
 * @author Frank
 *
 */
public interface SWCConfiguration extends RacingProcedureConfiguration {
    
    List<Flags> getStartModeFlags();

}
