package com.sap.sailing.domain.abstractlog.race.state.racingprocedure;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public interface RacingProcedureFactory {
    
    ReadonlyRacingProcedure createRacingProcedure(RacingProcedureType type, RaceLog raceLog);
    
    RegattaConfiguration getConfiguration();

}
