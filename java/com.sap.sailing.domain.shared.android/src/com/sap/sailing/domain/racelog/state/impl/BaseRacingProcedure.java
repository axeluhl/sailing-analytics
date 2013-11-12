package com.sap.sailing.domain.racelog.state.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.state.RacingProcedure2;

public abstract class BaseRacingProcedure implements RacingProcedure2 {

    private final RaceLog raceLog;

    public BaseRacingProcedure(RaceLog raceLog) {
        this.raceLog = raceLog;
    }

    public RaceLog getRaceLog() {
        return raceLog;
    }
    
}
