package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;

public interface RacingProcedureFactory {
    
    ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog);
    
    RegattaConfiguration getConfiguration();

}
